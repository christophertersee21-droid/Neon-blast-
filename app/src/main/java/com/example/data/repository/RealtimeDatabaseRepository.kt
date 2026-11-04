package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Announcement
import com.example.data.model.AppConfig
import com.example.data.model.AppNotification
import com.example.data.model.GameSession
import com.example.data.model.TaskItem
import com.example.data.model.TransactionItem
import com.example.data.model.UserProfile
import com.example.data.model.UserTaskProgress
import com.example.data.model.WithdrawalRequest
import com.example.data.model.isUserAdminEmail
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

open class RealtimeDatabaseRepository(
    val db: FirebaseDatabase = FirebaseDatabase.getInstance("https://com-aistudio-neonblast-r-d5875-default-rtdb.firebaseio.com/")
) {
    companion object {
        const val DATABASE_URL = "https://com-aistudio-neonblast-r-d5875-default-rtdb.firebaseio.com/"
        private const val TAG = "RTDBRepository"
    }

    constructor(context: Context) : this(
        FirebaseDatabase.getInstance(DATABASE_URL)
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Realtime Database.")
    }

    fun currentUserIdOrNull(): String? = auth.currentUser?.uid

    // ==================== APP CONFIG (ADMIN REMOTE CONTROL) ====================

    fun observeAppConfig(): Flow<AppConfig> = callbackFlow {
        val ref = db.getReference("app_config")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val config = snapshot.getValue(AppConfig::class.java) ?: AppConfig()
                trySend(config)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeAppConfig cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getAppConfig(): AppConfig {
        val snap = db.getReference("app_config").get().await()
        return snap.getValue(AppConfig::class.java) ?: AppConfig().also {
            // Seed initial config if empty
            db.getReference("app_config").setValue(it).await()
        }
    }

    suspend fun updateAppConfig(config: AppConfig): Result<Unit> {
        return try {
            val updated = config.copy(updatedAt = System.currentTimeMillis())
            db.getReference("app_config").setValue(updated).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update app config: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== USER PROFILE ====================

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val ref = db.getReference("users").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var profile = snapshot.getValue(UserProfile::class.java)
                if (profile != null) {
                    // Check if user is in admin emails list and guarantee admin status
                    if (isUserAdminEmail(profile.email) && !profile.isAdmin) {
                        profile = profile.copy(isAdmin = true)
                    }
                }
                trySend(profile)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeUserProfile cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getOrCreateUserProfile(email: String, displayName: String, photoUrl: String): UserProfile {
        val uid = requireUserId()
        val userRef = db.getReference("users").child(uid)
        val snap = userRef.get().await()

        val isAdmin = isUserAdminEmail(email)

        if (snap.exists()) {
            var existing = snap.getValue(UserProfile::class.java) ?: UserProfile(userId = uid, email = email, username = displayName)
            if (isAdmin && !existing.isAdmin) {
                existing = existing.copy(isAdmin = true)
                userRef.child("isAdmin").setValue(true).await()
            }
            return existing
        }

        val referralCode = "NB" + uid.takeLast(6).uppercase()
        val newProfile = UserProfile(
            id = uid,
            userId = uid,
            username = if (displayName.isNotBlank()) displayName else "Player_${uid.take(4)}",
            email = email,
            photoUrl = photoUrl,
            referralCode = referralCode,
            gameCoins = 500L, // Starter bonus
            eligibleBalance = 150.0, // Starter ₦150 Naira for testing instant withdrawal
            dailyStreak = 1L,
            isAdmin = isAdmin,
            country = "NG",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        userRef.setValue(newProfile).await()
        seedInitialTasksIfEmpty()
        return newProfile
    }

    suspend fun updateUsername(newUsername: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.getReference("users").child(uid).child("username").setValue(newUsername).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== DAILY LOGIN BONUS ====================

    suspend fun claimDailyBonus(userId: String): Result<Long> {
        return try {
            val userRef = db.getReference("users").child(userId)
            val snap = userRef.get().await()
            val user = snap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (user.lastLoginDate == today) {
                return Result.failure(Exception("Daily bonus already claimed today"))
            }

            val config = getAppConfig()
            val bonusCoins = config.dailyRewardCoins
            val newCoins = user.gameCoins + bonusCoins
            val newStreak = user.dailyStreak + 1

            userRef.child("gameCoins").setValue(newCoins).await()
            userRef.child("dailyStreak").setValue(newStreak).await()
            userRef.child("lastLoginDate").setValue(today).await()

            // Record transaction
            val txId = UUID.randomUUID().toString()
            val tx = TransactionItem(
                id = txId,
                userId = userId,
                type = "GAME_COIN_REWARD",
                amount = bonusCoins.toDouble(),
                currency = "COINS",
                balanceAfter = newCoins.toDouble(),
                status = "COMPLETED",
                description = "Daily Login Streak Bonus (Day $newStreak)"
            )
            db.getReference("transactions").child(userId).child(txId).setValue(tx).await()

            Result.success(bonusCoins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== GAME SESSION & SCORE ====================

    suspend fun saveGameSession(
        score: Long,
        linesCleared: Long,
        combos: Long,
        durationSeconds: Long
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userRef = db.getReference("users").child(uid)
            val snap = userRef.get().await()
            val user = snap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            val sessionId = UUID.randomUUID().toString()
            val session = GameSession(
                id = sessionId,
                sessionId = sessionId,
                userId = uid,
                score = score,
                linesCleared = linesCleared,
                combos = combos,
                durationSeconds = durationSeconds,
                createdAt = System.currentTimeMillis()
            )
            db.getReference("game_sessions").child(uid).child(sessionId).setValue(session).await()

            // Coin calculation: 1 coin per 10 points + combo bonus
            val earnedCoins = (score / 10).coerceAtLeast(5L) + (combos * 5L)
            val newBestScore = maxOf(user.bestScore, score)
            val newGamesPlayed = user.gamesPlayed + 1
            val newLines = user.linesCleared + linesCleared
            val newCoins = user.gameCoins + earnedCoins

            // Performance reward: bonus ₦2 Naira for scores over 500
            val nairaBonus = if (score >= 500) 2.0 else 0.0
            val newEligible = user.eligibleBalance + nairaBonus

            userRef.child("bestScore").setValue(newBestScore).await()
            userRef.child("gamesPlayed").setValue(newGamesPlayed).await()
            userRef.child("linesCleared").setValue(newLines).await()
            userRef.child("gameCoins").setValue(newCoins).await()
            if (nairaBonus > 0) {
                userRef.child("eligibleBalance").setValue(newEligible).await()
            }

            // Update user tasks progress
            updateTaskProgress(uid, "play_game", 1L)
            updateTaskProgress(uid, "clear_lines", linesCleared)
            if (score >= 1000) updateTaskProgress(uid, "reach_score_1000", 1L)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordGameSession(
        score: Long,
        linesCleared: Long,
        combos: Long,
        durationSeconds: Long
    ): Result<Unit> = saveGameSession(score, linesCleared, combos, durationSeconds)

    suspend fun deductCoinsForSecondChance(cost: Long): Boolean {
        return try {
            val uid = requireUserId()
            val userRef = db.getReference("users").child(uid)
            val snap = userRef.get().await()
            val user = snap.getValue(UserProfile::class.java) ?: return false
            if (user.gameCoins >= cost) {
                userRef.child("gameCoins").setValue(user.gameCoins - cost).await()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    // ==================== AD REWARDS ====================

    suspend fun rewardUserForWatchingAd(userId: String): Result<Pair<Long, Double>> {
        return try {
            val config = getAppConfig()
            val userRef = db.getReference("users").child(userId)
            val snap = userRef.get().await()
            val user = snap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            val newCoins = user.gameCoins + config.rewardedAdCoins
            val newNaira = user.eligibleBalance + config.rewardedAdNgn

            userRef.child("gameCoins").setValue(newCoins).await()
            userRef.child("eligibleBalance").setValue(newNaira).await()

            val txId = UUID.randomUUID().toString()
            val tx = TransactionItem(
                id = txId,
                userId = userId,
                type = "AD_REWARD",
                amount = config.rewardedAdNgn,
                currency = "NGN",
                balanceAfter = newNaira,
                status = "COMPLETED",
                description = "AdMob Video Reward (+${config.rewardedAdCoins} coins, +₦${config.rewardedAdNgn})"
            )
            db.getReference("transactions").child(userId).child(txId).setValue(tx).await()

            // Notification
            val notifId = UUID.randomUUID().toString()
            val notif = AppNotification(
                id = notifId,
                userId = userId,
                title = "AdMob Reward Credited! 🎬",
                message = "You earned +${config.rewardedAdCoins} Coins and ₦${config.rewardedAdNgn} Naira for watching an ad.",
                type = "AD_REWARD",
                createdAt = System.currentTimeMillis()
            )
            db.getReference("notifications").child(userId).child(notifId).setValue(notif).await()

            Result.success(Pair(config.rewardedAdCoins, config.rewardedAdNgn))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== TASKS & MISSIONS ====================

    fun observeTasks(): Flow<List<TaskItem>> = callbackFlow {
        val ref = db.getReference("tasks")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<TaskItem>()
                for (child in snapshot.children) {
                    child.getValue(TaskItem::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeTasks cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeUserTasks(userId: String): Flow<List<UserTaskProgress>> = callbackFlow {
        val ref = db.getReference("user_tasks").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UserTaskProgress>()
                for (child in snapshot.children) {
                    child.getValue(UserTaskProgress::class.java)?.let { list.add(it) }
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeUserTasks cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addOrUpdateTask(task: TaskItem): Result<Unit> {
        return try {
            val id = if (task.id.isNotBlank()) task.id else UUID.randomUUID().toString()
            db.getReference("tasks").child(id).setValue(task.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            db.getReference("tasks").child(taskId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateTaskProgress(userId: String, taskId: String, progressIncrement: Long) {
        try {
            val taskRef = db.getReference("tasks").child(taskId)
            val taskSnap = taskRef.get().await()
            val task = taskSnap.getValue(TaskItem::class.java) ?: return

            val userTaskRef = db.getReference("user_tasks").child(userId).child(taskId)
            val userTaskSnap = userTaskRef.get().await()
            val current = userTaskSnap.getValue(UserTaskProgress::class.java)
                ?: UserTaskProgress(id = taskId, userId = userId, taskId = taskId, target = task.target)

            val newProgress = (current.currentProgress + progressIncrement).coerceAtMost(task.target)
            val completed = newProgress >= task.target

            userTaskRef.setValue(
                current.copy(
                    currentProgress = newProgress,
                    isCompleted = completed,
                    updatedAt = System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update task progress: ${e.message}")
        }
    }

    suspend fun claimTaskReward(userId: String, taskId: String): Result<Unit> {
        return try {
            val taskSnap = db.getReference("tasks").child(taskId).get().await()
            val task = taskSnap.getValue(TaskItem::class.java) ?: return Result.failure(Exception("Task not found"))

            val userTaskRef = db.getReference("user_tasks").child(userId).child(taskId)
            val userTaskSnap = userTaskRef.get().await()
            val userTask = userTaskSnap.getValue(UserTaskProgress::class.java) ?: return Result.failure(Exception("Task not started"))

            if (!userTask.isCompleted) return Result.failure(Exception("Task not yet completed"))
            if (userTask.isClaimed) return Result.failure(Exception("Task reward already claimed"))

            val userRef = db.getReference("users").child(userId)
            val userSnap = userRef.get().await()
            val user = userSnap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            val newCoins = user.gameCoins + task.rewardCoins
            val newNaira = user.eligibleBalance + task.rewardEligible

            userRef.child("gameCoins").setValue(newCoins).await()
            if (task.rewardEligible > 0) {
                userRef.child("eligibleBalance").setValue(newNaira).await()
            }
            userTaskRef.child("isClaimed").setValue(true).await()

            // Record transaction
            val txId = UUID.randomUUID().toString()
            val tx = TransactionItem(
                id = txId,
                userId = userId,
                type = "TASK_REWARD",
                amount = task.rewardCoins.toDouble(),
                currency = if (task.rewardEligible > 0) "NGN" else "COINS",
                balanceAfter = newCoins.toDouble(),
                status = "COMPLETED",
                description = "Completed Task: ${task.title}"
            )
            db.getReference("transactions").child(userId).child(txId).setValue(tx).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun claimTaskReward(task: TaskItem): Result<Unit> = claimTaskReward(requireUserId(), task.id)

    suspend fun seedDefaultsIfNeeded() {
        seedInitialTasksIfEmpty()
        getAppConfig()
    }

    private suspend fun seedInitialTasksIfEmpty() {
        try {
            val tasksSnap = db.getReference("tasks").get().await()
            if (!tasksSnap.exists() || tasksSnap.childrenCount == 0L) {
                val defaults = listOf(
                    TaskItem("play_game", "Play Neon Puzzle", "Play 1 complete game session", "daily", 1L, 50L, 5.0),
                    TaskItem("clear_lines", "Neon Line Destroyer", "Clear 10 lines in any game", "daily", 10L, 100L, 10.0),
                    TaskItem("reach_score_1000", "High Scorer", "Reach a game score of 1,000", "daily", 1L, 150L, 15.0),
                    TaskItem("invite_friend", "Spread Neon Blast", "Invite a friend with your code", "referral", 1L, 200L, 25.0)
                )
                for (t in defaults) {
                    db.getReference("tasks").child(t.id).setValue(t).await()
                }
            }
        } catch (_: Exception) {}
    }

    // ==================== WALLET & NIGERIAN WITHDRAWALS ====================

    fun observeTransactions(userId: String): Flow<List<TransactionItem>> = callbackFlow {
        val ref = db.getReference("transactions").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<TransactionItem>()
                for (child in snapshot.children) {
                    child.getValue(TransactionItem::class.java)?.let { list.add(it) }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeTransactions cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeWithdrawals(userId: String): Flow<List<WithdrawalRequest>> = callbackFlow {
        val ref = db.getReference("withdrawals")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<WithdrawalRequest>()
                for (child in snapshot.children) {
                    val w = child.getValue(WithdrawalRequest::class.java)
                    if (w != null && w.userId == userId) {
                        list.add(w)
                    }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeWithdrawals cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeAllWithdrawals(): Flow<List<WithdrawalRequest>> = callbackFlow {
        val ref = db.getReference("withdrawals")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<WithdrawalRequest>()
                for (child in snapshot.children) {
                    child.getValue(WithdrawalRequest::class.java)?.let { list.add(it) }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeAllWithdrawals cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun requestWithdrawal(
        userId: String,
        userEmail: String,
        amount: Double,
        bankName: String,
        accountNumber: String,
        accountName: String
    ): Result<String> {
        return try {
            val config = getAppConfig()
            if (amount < config.minWithdrawalNgn) {
                return Result.failure(Exception("Minimum withdrawal is ₦${config.minWithdrawalNgn} Naira"))
            }

            if (accountNumber.length != 10 || !accountNumber.all { it.isDigit() }) {
                return Result.failure(Exception("Please enter a valid 10-digit Nigerian NUBAN account number"))
            }

            if (accountName.isBlank()) {
                return Result.failure(Exception("Please enter the account holder's name"))
            }

            val userRef = db.getReference("users").child(userId)
            val snap = userRef.get().await()
            val user = snap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            if (user.eligibleBalance < amount) {
                return Result.failure(Exception("Insufficient eligible balance. You have ₦${user.eligibleBalance}."))
            }

            val withdrawalId = UUID.randomUUID().toString()
            val reference = "NB-NGN-${System.currentTimeMillis().toString().takeLast(8)}"
            val payoutDetails = "$bankName | $accountNumber | $accountName"

            val withdrawal = WithdrawalRequest(
                id = withdrawalId,
                userId = userId,
                userEmail = userEmail,
                userName = user.username,
                amount = amount,
                currency = "NGN",
                payoutMethod = "Nigerian Bank Transfer",
                bankName = bankName,
                accountNumber = accountNumber,
                accountName = accountName,
                payoutDetails = payoutDetails,
                status = "PENDING",
                reference = reference,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Deduct balance and add to pending
            val newBalance = user.eligibleBalance - amount
            val newPending = user.pendingWithdrawal + amount
            userRef.child("eligibleBalance").setValue(newBalance).await()
            userRef.child("pendingWithdrawal").setValue(newPending).await()

            // Save withdrawal request
            db.getReference("withdrawals").child(withdrawalId).setValue(withdrawal).await()

            // Save transaction record
            val txId = UUID.randomUUID().toString()
            val tx = TransactionItem(
                id = txId,
                userId = userId,
                type = "WITHDRAWAL_REQUEST",
                amount = -amount,
                currency = "NGN",
                balanceAfter = newBalance,
                status = "PENDING",
                description = "Withdrawal to $bankName ($accountNumber)",
                reference = reference
            )
            db.getReference("transactions").child(userId).child(txId).setValue(tx).await()

            // In-app notification
            val notifId = UUID.randomUUID().toString()
            val notif = AppNotification(
                id = notifId,
                userId = userId,
                title = "Withdrawal Submitted ⏳",
                message = "Your withdrawal of ₦$amount to $bankName is submitted and awaiting admin approval.",
                type = "WITHDRAWAL"
            )
            db.getReference("notifications").child(userId).child(notifId).setValue(notif).await()

            Result.success(reference)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ADMIN ACTIONS ====================

    suspend fun approveWithdrawal(withdrawalId: String, paymentReference: String): Result<Unit> {
        return try {
            val wRef = db.getReference("withdrawals").child(withdrawalId)
            val snap = wRef.get().await()
            val w = snap.getValue(WithdrawalRequest::class.java) ?: return Result.failure(Exception("Withdrawal not found"))

            if (w.status != "PENDING") {
                return Result.failure(Exception("Withdrawal is already ${w.status}"))
            }

            val userRef = db.getReference("users").child(w.userId)
            val userSnap = userRef.get().await()
            val user = userSnap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            val newPending = (user.pendingWithdrawal - w.amount).coerceAtLeast(0.0)
            val newTotalWithdrawn = user.totalWithdrawn + w.amount
            userRef.child("pendingWithdrawal").setValue(newPending).await()
            userRef.child("totalWithdrawn").setValue(newTotalWithdrawn).await()

            wRef.child("status").setValue("APPROVED").await()
            wRef.child("paymentReference").setValue(paymentReference).await()
            wRef.child("updatedAt").setValue(System.currentTimeMillis()).await()

            // Transaction
            val txId = UUID.randomUUID().toString()
            val tx = TransactionItem(
                id = txId,
                userId = w.userId,
                type = "WITHDRAWAL_APPROVED",
                amount = w.amount,
                currency = "NGN",
                balanceAfter = user.eligibleBalance,
                status = "APPROVED",
                description = "Paid to ${w.bankName} (${w.accountNumber}). Ref: $paymentReference",
                reference = paymentReference
            )
            db.getReference("transactions").child(w.userId).child(txId).setValue(tx).await()

            // Notification
            val notifId = UUID.randomUUID().toString()
            val notif = AppNotification(
                id = notifId,
                userId = w.userId,
                title = "Withdrawal Approved! 🎉",
                message = "Your ₦${w.amount} withdrawal to ${w.bankName} was approved. Payment ref: $paymentReference",
                type = "WITHDRAWAL"
            )
            db.getReference("notifications").child(w.userId).child(notifId).setValue(notif).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectWithdrawal(withdrawalId: String, reason: String): Result<Unit> {
        return try {
            val wRef = db.getReference("withdrawals").child(withdrawalId)
            val snap = wRef.get().await()
            val w = snap.getValue(WithdrawalRequest::class.java) ?: return Result.failure(Exception("Withdrawal not found"))

            if (w.status != "PENDING") {
                return Result.failure(Exception("Withdrawal is already ${w.status}"))
            }

            val userRef = db.getReference("users").child(w.userId)
            val userSnap = userRef.get().await()
            val user = userSnap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            // Refund balance and deduct pending
            val restoredBalance = user.eligibleBalance + w.amount
            val newPending = (user.pendingWithdrawal - w.amount).coerceAtLeast(0.0)
            userRef.child("eligibleBalance").setValue(restoredBalance).await()
            userRef.child("pendingWithdrawal").setValue(newPending).await()

            wRef.child("status").setValue("REJECTED").await()
            wRef.child("rejectionReason").setValue(reason).await()
            wRef.child("updatedAt").setValue(System.currentTimeMillis()).await()

            // Transaction
            val txId = UUID.randomUUID().toString()
            val tx = TransactionItem(
                id = txId,
                userId = w.userId,
                type = "WITHDRAWAL_REJECTED",
                amount = w.amount,
                currency = "NGN",
                balanceAfter = restoredBalance,
                status = "REJECTED",
                description = "Withdrawal rejected & refunded: $reason",
                reference = w.reference
            )
            db.getReference("transactions").child(w.userId).child(txId).setValue(tx).await()

            // Notification
            val notifId = UUID.randomUUID().toString()
            val notif = AppNotification(
                id = notifId,
                userId = w.userId,
                title = "Withdrawal Refunded ↩️",
                message = "Withdrawal of ₦${w.amount} was rejected ($reason). Funds have been returned to your balance.",
                type = "WITHDRAWAL"
            )
            db.getReference("notifications").child(w.userId).child(notifId).setValue(notif).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAllUsers(): Flow<List<UserProfile>> = callbackFlow {
        val ref = db.getReference("users")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UserProfile>()
                for (child in snapshot.children) {
                    var u = child.getValue(UserProfile::class.java)
                    if (u != null) {
                        if (isUserAdminEmail(u.email)) {
                            u = u.copy(isAdmin = true)
                        }
                        list.add(u)
                    }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeAllUsers cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun adminAdjustUserBalance(
        targetUserId: String,
        coinsDelta: Long,
        nairaDelta: Double,
        reason: String
    ): Result<Unit> {
        return try {
            val userRef = db.getReference("users").child(targetUserId)
            val snap = userRef.get().await()
            val user = snap.getValue(UserProfile::class.java) ?: return Result.failure(Exception("User not found"))

            val newCoins = (user.gameCoins + coinsDelta).coerceAtLeast(0L)
            val newNaira = (user.eligibleBalance + nairaDelta).coerceAtLeast(0.0)

            userRef.child("gameCoins").setValue(newCoins).await()
            userRef.child("eligibleBalance").setValue(newNaira).await()

            val txId = UUID.randomUUID().toString()
            val tx = TransactionItem(
                id = txId,
                userId = targetUserId,
                type = "ADMIN_ADJUSTMENT",
                amount = if (nairaDelta != 0.0) nairaDelta else coinsDelta.toDouble(),
                currency = if (nairaDelta != 0.0) "NGN" else "COINS",
                balanceAfter = newNaira,
                status = "COMPLETED",
                description = "Admin Adjustment: $reason"
            )
            db.getReference("transactions").child(targetUserId).child(txId).setValue(tx).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== NOTIFICATIONS & ANNOUNCEMENTS ====================

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val ref = db.getReference("notifications").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<AppNotification>()
                for (child in snapshot.children) {
                    child.getValue(AppNotification::class.java)?.let { list.add(it) }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeNotifications cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun markNotificationRead(userId: String, notificationId: String) {
        try {
            db.getReference("notifications").child(userId).child(notificationId).child("read").setValue(true).await()
        } catch (_: Exception) {}
    }

    suspend fun markNotificationRead(notificationId: String) {
        val uid = currentUserIdOrNull() ?: return
        markNotificationRead(uid, notificationId)
    }

    fun observeAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val ref = db.getReference("announcements")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Announcement>()
                for (child in snapshot.children) {
                    child.getValue(Announcement::class.java)?.let { list.add(it) }
                }
                list.sortByDescending { it.createdAt }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeAnnouncements cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            val id = if (announcement.id.isNotBlank()) announcement.id else UUID.randomUUID().toString()
            db.getReference("announcements").child(id).setValue(announcement.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
