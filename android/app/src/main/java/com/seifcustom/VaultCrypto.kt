package com.seifcustom

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Local AES-256-GCM encryption primitive; not a complete vault UI or recovery system. */
object VaultCrypto {
    private const val STORE = "AndroidKeyStore"
    private const val ALIAS = "seif_custom_vault_aes_gcm"
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance(STORE).apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, STORE)
        generator.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build())
        return generator.generateKey()
    }
    fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key())
        val iv = cipher.iv; val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(4 + iv.size + encrypted.size).putInt(iv.size).put(iv).put(encrypted).array()
    }
    fun decrypt(payload: ByteArray): ByteArray {
        val buffer = ByteBuffer.wrap(payload); val ivLength = buffer.int
        require(ivLength in 12..16 && buffer.remaining() > ivLength) { "Invalid encrypted vault payload" }
        val iv = ByteArray(ivLength).also(buffer::get)
        val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }
}
