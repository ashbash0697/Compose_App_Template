package com.example.core.utils

import android.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class AESEncryption {

    companion object {
        private const val pswdIterations = 10
        private const val keySize = 128
        private const val cypherInstance = "AES/CBC/PKCS5Padding"
        private const val secretKeyInstance = "PBKDF2WithHmacSHA1"
        private const val plainText = "Holofytext"
        private const val AESSalt = "holofysalt"
        private const val initializationVector = "8119745113154120"

        @Throws(Exception::class)
        fun encrypt(textToEncrypt: String?): String? {
            if (textToEncrypt == null)
                return null

            val skeySpec = SecretKeySpec(getRaw(plainText, AESSalt), "AES")
            val cipher: Cipher = Cipher.getInstance(cypherInstance)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                skeySpec,
                IvParameterSpec(initializationVector.toByteArray())
            )
            val encrypted: ByteArray = cipher.doFinal(textToEncrypt.toByteArray())
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        }

        @Throws(Exception::class)
        fun decrypt(textToDecrypt: String?): String? {
            if (textToDecrypt == null)
                return null
            val encryted_bytes: ByteArray = Base64.decode(textToDecrypt, Base64.DEFAULT)
            val skeySpec = SecretKeySpec(getRaw(plainText, AESSalt), "AES")
            val cipher: Cipher = Cipher.getInstance(cypherInstance)
            cipher.init(
                Cipher.DECRYPT_MODE,
                skeySpec,
                IvParameterSpec(initializationVector.toByteArray())
            )
            val decrypted: ByteArray = cipher.doFinal(encryted_bytes)
            return String(decrypted, Charsets.UTF_8)
        }

        private fun getRaw(plainText: String, salt: String): ByteArray? {
            try {
                val factory: SecretKeyFactory = SecretKeyFactory.getInstance(secretKeyInstance)
                val spec: KeySpec =
                    PBEKeySpec(plainText.toCharArray(), salt.toByteArray(), pswdIterations, keySize)
                return factory.generateSecret(spec).getEncoded()
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return ByteArray(0)
        }
    }

}