package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Announcement
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.GameSession
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.example.data.model.UserProfile
import com.example.data.model.UserTaskProgress
import com.example.data.model.WithdrawalRequest
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun currentUserIdOrNull(): String? = auth.currentUser?.uid

    // ==================== USER PROFILE ====================

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snap -> snap.toObject<UserProfile>() }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                emit(null)
            }
    }

    suspend fun getOrCreateUserProfile(email: String, displayName: String, photoUrl: String): UserProfile {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        if (snap.exists()) {
            return snap.toObject<UserProfile>() ?: UserProfile(userId = uid, email = email, username = displayName)
        }

        val referralCode = "NB" + uid.takeLast(6).uppercase()
        val newProfile = UserProfile(
            id = uid,
            userId = uid,
            username = if (displayName.isNotBlank()) displayName else "Player_${uid.take(4)}",
            email = email,
            photoUrl = photoUrl,
            referralCode = referralCode,
            gameCoins = 250L, // welcome starter bonus
            eligibleBalance = 5.0, // starter eligible reward balance for testing
            dailyStreak = 1L
        )

        val data = mapOf(
            "userId" to uid,
            "username" to newProfile.username,
            "email" to email,
            "photoUrl" to photoUrl,
            "referralCode" to referralCode,
            "bestScore" to 0L,
            "gamesPlayed" to 0L,
            "linesCleared" to 0L,
            "gameCoins" to 250L,
            "eligibleBalance" to 5.0,
            "pendingWithdrawal" to 0.0,
            "totalWithdrawn" to 0.0,
            "dailyStreak" to 1L,
            "lastLoginDate" to "",
            "isAdmin" to false,
            "isSuspended" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()

        // Log welcome starter transaction
        createTransaction(
            TransactionItem(
                userId = uid,
                type = "WELCOME_BONUS",
                amount = 5.0,
                currency = "USD",
                balanceAfter = 5.0,
                status = "COMPLETED",
                description = "Welcome bonus credited to eligible balance",
                reference = "TX-INIT-$uid"
            )
        )

        return newProfile
    }

    suspend fun claimDailyBonus(todayDateStr: String): Long {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snap = docRef.get().await()
        val user = snap.toObject<UserProfile>() ?: return 0L
        if (user.lastLoginDate == todayDateStr) {
            return 0L // Already claimed today
        }

        val bonusCoins = 100L
        docRef.update(
            mapOf(
                "gameCoins" to FieldValue.increment(bonusCoins),
                "dailyStreak" to FieldValue.increment(1L),
                "lastLoginDate" to todayDateStr,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return bonusCoins
    }

    // ==================== GAME SESSIONS & PROGRESS ====================

    suspend fun recordGameSession(
        score: Long,
        linesCleared: Long,
        combos: Long,
        durationSeconds: Long
    ) {
        val uid = requireUserId()
        val sessionId = UUID.randomUUID().toString()
        val sessionRef = db.collection("game_sessions").document(sessionId)

        val sessionPayload = mapOf(
            "sessionId" to sessionId,
            "userId" to uid,
            "score" to score,
            "linesCleared" to linesCleared,
            "combos" to combos,
            "durationSeconds" to durationSeconds,
            "createdAt" to FieldValue.serverTimestamp()
        )
        sessionRef.set(sessionPayload).await()

        // Update user stats
        val userRef = db.collection("users").document(uid)
        val userSnap = userRef.get().await()
        val currentBest = userSnap.getLong("bestScore") ?: 0L
        val updates = mutableMapOf<String, Any>(
            "gamesPlayed" to FieldValue.increment(1L),
            "linesCleared" to FieldValue.increment(linesCleared),
            "gameCoins" to FieldValue.increment(score / 10L),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (score > currentBest) {
            updates["bestScore"] = score
        }
        userRef.update(updates).await()

        // Update default daily tasks progress
        updateTaskProgress("daily_play", 1L)
        updateTaskProgress("daily_lines", linesCleared)
        if (score >= 1000L) {
            updateTaskProgress("daily_score_1000", score)
        }
    }

    suspend fun deductCoinsForSecondChance(cost: Long): Boolean {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val snap = userRef.get().await()
        val currentCoins = snap.getLong("gameCoins") ?: 0L
        if (currentCoins < cost) return false

        userRef.update(
            mapOf(
                "gameCoins" to FieldValue.increment(-cost),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return true
    }

    // ==================== TASKS ====================

    fun observeTasks(): Flow<List<TaskItem>> {
        return db.collection("tasks")
            .whereEqualTo("isActive", true)
            .snapshots()
            .map { it.toObjects<TaskItem>() }
            .catch { emit(emptyList()) }
    }

    fun observeUserTasks(userId: String): Flow<List<UserTaskProgress>> {
        return db.collection("user_tasks")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { it.toObjects<UserTaskProgress>() }
            .catch { emit(emptyList()) }
    }

    suspend fun updateTaskProgress(taskId: String, incrementBy: Long) {
        val uid = requireUserId()
        val docId = "${uid}_$taskId"
        val docRef = db.collection("user_tasks").document(docId)
        val snap = docRef.get().await()

        if (!snap.exists()) {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "taskId" to taskId,
                    "currentProgress" to incrementBy,
                    "target" to 10L,
                    "isCompleted" to false,
                    "isClaimed" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } else {
            val current = snap.getLong("currentProgress") ?: 0L
            val target = snap.getLong("target") ?: 10L
            val newProgress = current + incrementBy
            docRef.update(
                mapOf(
                    "currentProgress" to newProgress,
                    "isCompleted" to (newProgress >= target),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun claimTaskReward(task: TaskItem): Boolean {
        val uid = requireUserId()
        val docId = "${uid}_${task.id}"
        val userTaskRef = db.collection("user_tasks").document(docId)
        val snap = userTaskRef.get().await()
        if (snap.getBoolean("isClaimed") == true) return false

        userTaskRef.update(
            mapOf(
                "isClaimed" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val userRef = db.collection("users").document(uid)
        val userUpdates = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (task.rewardCoins > 0) {
            userUpdates["gameCoins"] = FieldValue.increment(task.rewardCoins)
        }
        if (task.rewardEligible > 0) {
            userUpdates["eligibleBalance"] = FieldValue.increment(task.rewardEligible)
        }
        userRef.update(userUpdates).await()

        if (task.rewardEligible > 0) {
            val userSnap = userRef.get().await()
            val newBalance = userSnap.getDouble("eligibleBalance") ?: 0.0
            createTransaction(
                TransactionItem(
                    userId = uid,
                    type = "TASK_REWARD",
                    amount = task.rewardEligible,
                    currency = "USD",
                    balanceAfter = newBalance,
                    status = "COMPLETED",
                    description = "Claimed reward for ${task.title}",
                    reference = "TX-TASK-${task.id}"
                )
            )
        }
        return true
    }

    // ==================== WALLET & TRANSACTIONS ====================

    fun observeTransactions(userId: String): Flow<List<TransactionItem>> {
        return db.collection("transactions")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects<TransactionItem>().sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun createTransaction(transaction: TransactionItem) {
        val txId = UUID.randomUUID().toString()
        val txRef = db.collection("transactions").document(txId)
        val payload = mapOf(
            "userId" to transaction.userId,
            "type" to transaction.type,
            "amount" to transaction.amount,
            "currency" to transaction.currency,
            "balanceAfter" to transaction.balanceAfter,
            "status" to transaction.status,
            "description" to transaction.description,
            "reference" to if (transaction.reference.isNotBlank()) transaction.reference else "TX-$txId",
            "createdAt" to FieldValue.serverTimestamp()
        )
        txRef.set(payload).await()
    }

    // ==================== WITHDRAWAL WORKFLOW ====================

    fun observeWithdrawals(userId: String): Flow<List<WithdrawalRequest>> {
        return db.collection("withdrawals")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects<WithdrawalRequest>().sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun requestWithdrawal(
        amount: Double,
        payoutMethod: String,
        payoutDetails: String,
        userEmail: String
    ): Result<String> {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val userSnap = userRef.get().await()
        val currentEligible = userSnap.getDouble("eligibleBalance") ?: 0.0

        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be greater than zero."))
        if (currentEligible < amount) return Result.failure(IllegalStateException("Insufficient eligible balance ($$currentEligible available)."))

        val withdrawalId = UUID.randomUUID().toString()
        val reference = "NB-W-" + UUID.randomUUID().toString().take(8).uppercase()

        // Atomic reservation: deduct from eligibleBalance and add to pendingWithdrawal
        userRef.update(
            mapOf(
                "eligibleBalance" to FieldValue.increment(-amount),
                "pendingWithdrawal" to FieldValue.increment(amount),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val withdrawalRef = db.collection("withdrawals").document(withdrawalId)
        val withdrawalPayload = mapOf(
            "userId" to uid,
            "userEmail" to userEmail,
            "amount" to amount,
            "currency" to "USD",
            "payoutMethod" to payoutMethod,
            "payoutDetails" to payoutDetails,
            "status" to "PENDING",
            "reference" to reference,
            "rejectionReason" to "",
            "paymentReference" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        withdrawalRef.set(withdrawalPayload).await()

        createTransaction(
            TransactionItem(
                userId = uid,
                type = "WITHDRAWAL_REQUEST",
                amount = -amount,
                currency = "USD",
                balanceAfter = currentEligible - amount,
                status = "PENDING",
                description = "Withdrawal request submitted via $payoutMethod",
                reference = reference
            )
        )

        return Result.success(reference)
    }

    // ==================== ADMIN DASHBOARD ====================

    fun observeAllUsers(): Flow<List<UserProfile>> {
        return db.collection("users")
            .snapshots()
            .map { it.toObjects<UserProfile>() }
            .catch { emit(emptyList()) }
    }

    fun observeAllWithdrawals(): Flow<List<WithdrawalRequest>> {
        return db.collection("withdrawals")
            .snapshots()
            .map { snap ->
                snap.toObjects<WithdrawalRequest>().sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun approveWithdrawal(withdrawalId: String): Boolean {
        val ref = db.collection("withdrawals").document(withdrawalId)
        val snap = ref.get().await()
        if (!snap.exists()) return false

        ref.update(
            mapOf(
                "status" to "APPROVED",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val userId = snap.getString("userId") ?: ""
        val reference = snap.getString("reference") ?: ""
        val amount = snap.getDouble("amount") ?: 0.0

        createNotification(
            userId = userId,
            title = "Withdrawal Approved",
            message = "Your withdrawal of $$amount ($reference) has been approved and is being processed.",
            type = "WITHDRAWAL"
        )
        return true
    }

    suspend fun markWithdrawalPaid(withdrawalId: String, paymentReference: String): Boolean {
        val ref = db.collection("withdrawals").document(withdrawalId)
        val snap = ref.get().await()
        if (!snap.exists()) return false

        val userId = snap.getString("userId") ?: ""
        val amount = snap.getDouble("amount") ?: 0.0
        val reference = snap.getString("reference") ?: ""

        ref.update(
            mapOf(
                "status" to "PAID",
                "paymentReference" to paymentReference,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // Deduct from pendingWithdrawal and add to totalWithdrawn
        val userRef = db.collection("users").document(userId)
        userRef.update(
            mapOf(
                "pendingWithdrawal" to FieldValue.increment(-amount),
                "totalWithdrawn" to FieldValue.increment(amount),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        createTransaction(
            TransactionItem(
                userId = userId,
                type = "WITHDRAWAL_PAID",
                amount = amount,
                currency = "USD",
                balanceAfter = 0.0,
                status = "PAID",
                description = "Payout sent via payment ref: $paymentReference",
                reference = reference
            )
        )

        createNotification(
            userId = userId,
            title = "Withdrawal Paid",
            message = "Funds of $$amount have been sent! Ref: $paymentReference",
            type = "WITHDRAWAL"
        )
        return true
    }

    suspend fun rejectWithdrawal(withdrawalId: String, reason: String): Boolean {
        val ref = db.collection("withdrawals").document(withdrawalId)
        val snap = ref.get().await()
        if (!snap.exists()) return false

        val userId = snap.getString("userId") ?: ""
        val amount = snap.getDouble("amount") ?: 0.0
        val reference = snap.getString("reference") ?: ""

        ref.update(
            mapOf(
                "status" to "REJECTED",
                "rejectionReason" to reason,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // Restore reserved balance!
        val userRef = db.collection("users").document(userId)
        userRef.update(
            mapOf(
                "eligibleBalance" to FieldValue.increment(amount),
                "pendingWithdrawal" to FieldValue.increment(-amount),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val restoredSnap = userRef.get().await()
        val newEligible = restoredSnap.getDouble("eligibleBalance") ?: 0.0

        // Create auditable reversal transaction
        createTransaction(
            TransactionItem(
                userId = userId,
                type = "WITHDRAWAL_REJECTED",
                amount = amount,
                currency = "USD",
                balanceAfter = newEligible,
                status = "REJECTED",
                description = "Withdrawal $reference rejected ($reason). Funds restored.",
                reference = "REV-$reference"
            )
        )

        createNotification(
            userId = userId,
            title = "Withdrawal Rejected",
            message = "Withdrawal ($reference) rejected: $reason. $$amount restored to available balance.",
            type = "WITHDRAWAL"
        )
        return true
    }

    suspend fun toggleUserSuspension(userId: String, isSuspended: Boolean) {
        db.collection("users").document(userId).update(
            mapOf(
                "isSuspended" to isSuspended,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun makeUserAdmin(userId: String, isAdmin: Boolean) {
        db.collection("users").document(userId).update(
            mapOf(
                "isAdmin" to isAdmin,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // ==================== ANNOUNCEMENTS & NOTIFICATIONS ====================

    fun observeAnnouncements(): Flow<List<Announcement>> {
        return db.collection("announcements")
            .whereEqualTo("isActive", true)
            .snapshots()
            .map { it.toObjects<Announcement>() }
            .catch { emit(emptyList()) }
    }

    suspend fun publishAnnouncement(title: String, content: String, badge: String) {
        val id = UUID.randomUUID().toString()
        db.collection("announcements").document(id).set(
            mapOf(
                "title" to title,
                "content" to content,
                "badge" to badge,
                "actionUrl" to "",
                "isActive" to true,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> {
        return db.collection("notifications")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects<AppNotification>().sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun createNotification(userId: String, title: String, message: String, type: String) {
        val notifId = UUID.randomUUID().toString()
        db.collection("notifications").document(notifId).set(
            mapOf(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "type" to type,
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun markNotificationRead(notificationId: String) {
        db.collection("notifications").document(notificationId).update(
            mapOf("isRead" to true)
        ).await()
    }

    // Seed default tasks and sample announcement if empty
    suspend fun seedDefaultsIfNeeded() {
        try {
            val tasksSnap = db.collection("tasks").limit(1).get().await()
            if (tasksSnap.isEmpty) {
                val defaults = listOf(
                    TaskItem(id = "daily_play", title = "Play 3 Games", description = "Complete 3 neon puzzle games today", category = "daily", target = 3L, rewardCoins = 50L, rewardEligible = 0.50),
                    TaskItem(id = "daily_lines", title = "Clear 20 Lines", description = "Clear 20 total rows or columns", category = "daily", target = 20L, rewardCoins = 80L, rewardEligible = 0.75),
                    TaskItem(id = "daily_score_1000", title = "Reach 1,000 Points", description = "Achieve a score of 1000 in a single match", category = "daily", target = 1000L, rewardCoins = 100L, rewardEligible = 1.00),
                    TaskItem(id = "weekly_play_20", title = "Weekly Master", description = "Play 20 games this week", category = "weekly", target = 20L, rewardCoins = 300L, rewardEligible = 3.00),
                    TaskItem(id = "referral_friend", title = "Invite a Friend", description = "Share your referral code with friends", category = "referral", target = 1L, rewardCoins = 200L, rewardEligible = 2.00)
                )
                for (t in defaults) {
                    db.collection("tasks").document(t.id).set(t).await()
                }
            }

            val annSnap = db.collection("announcements").limit(1).get().await()
            if (annSnap.isEmpty) {
                db.collection("announcements").document("welcome_ann").set(
                    mapOf(
                        "title" to "Welcome to Neon Blast!",
                        "content" to "Experience high-voltage block puzzle action. Earn coins, complete missions, and unlock rewards!",
                        "badge" to "NEW",
                        "actionUrl" to "",
                        "isActive" to true,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.BATCH, "seedDefaultsIfNeeded")
        }
    }
}
