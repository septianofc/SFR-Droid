package com.example.util

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val PREFIX = "ENC_V1:"

    fun generateRandomKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun deriveKey(passphrase: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(passphrase.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainText: String, secretKey: String): String {
        if (plainText.isEmpty() || secretKey.isEmpty()) return plainText
        return try {
            val keySpec = deriveKey(secretKey)
            val cipher = Cipher.getInstance(ALGORITHM)

            val ivBytes = ByteArray(16)
            SecureRandom().nextBytes(ivBytes)
            val ivSpec = IvParameterSpec(ivBytes)

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            val ivBase64 = Base64.encodeToString(ivBytes, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)

            "$PREFIX$ivBase64:$cipherBase64"
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(cipherPayload: String, secretKey: String): String {
        if (!cipherPayload.startsWith(PREFIX) || secretKey.isEmpty()) return cipherPayload
        return try {
            val stripped = cipherPayload.removePrefix(PREFIX)
            val parts = stripped.split(":")
            if (parts.size != 2) return cipherPayload

            val ivBytes = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)

            val keySpec = deriveKey(secretKey)
            val ivSpec = IvParameterSpec(ivBytes)

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decryptedBytes = cipher.doFinal(cipherBytes)

            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            cipherPayload
        }
    }

    fun getPhpDecryptionSnippet(keyPlaceholder: String = "KUNCI_ANDA"): String {
        return """
<?php
// Contoh Fungsi Dekripsi Payload / Token SFR Droid di PHP (AES-256-CBC)
function decryptSfrData(${'$'}encryptedString, ${'$'}secretKey) {
    if (strpos(${'$'}encryptedString, 'ENC_V1:') !== 0) {
        return ${'$'}encryptedString; // Tidak dienkripsi
    }
    ${'$'}raw = substr(${'$'}encryptedString, 7);
    ${'$'}parts = explode(':', ${'$'}raw, 2);
    if (count(${'$'}parts) !== 2) return ${'$'}encryptedString;
    
    ${'$'}iv = base64_decode(${'$'}parts[0]);
    ${'$'}cipherText = base64_decode(${'$'}parts[1]);
    ${'$'}derivedKey = hash('sha256', ${'$'}secretKey, true);
    
    return openssl_decrypt(${'$'}cipherText, 'aes-256-cbc', ${'$'}derivedKey, OPENSSL_RAW_DATA, ${'$'}iv);
}

// Cara Penggunaan:
// ${'$'}key = '$keyPlaceholder';
// ${'$'}input = file_get_contents('php://input');
// ${'$'}decryptedJson = decryptSfrData(${'$'}input, ${'$'}key);
// ${'$'}data = json_decode(${'$'}decryptedJson, true);
?>
        """.trimIndent()
    }

    fun getNodeJsDecryptionSnippet(keyPlaceholder: String = "KUNCI_ANDA"): String {
        return """
// Contoh Fungsi Dekripsi Payload / Token SFR Droid di Node.js (AES-256-CBC)
const crypto = require('crypto');

function decryptSfrData(encryptedString, secretKey) {
  if (!encryptedString.startsWith('ENC_V1:')) return encryptedString;
  const parts = encryptedString.substring(7).split(':');
  if (parts.length !== 2) return encryptedString;

  const iv = Buffer.from(parts[0], 'base64');
  const cipherText = Buffer.from(parts[1], 'base64');
  const derivedKey = crypto.createHash('sha256').update(secretKey, 'utf8').digest();

  const decipher = crypto.createDecipheriv('aes-256-cbc', derivedKey, iv);
  let decrypted = decipher.update(cipherText, undefined, 'utf8');
  decrypted += decipher.final('utf8');
  return decrypted;
}

// Cara Penggunaan di Express:
// const key = '$keyPlaceholder';
// const decryptedText = decryptSfrData(req.body, key);
        """.trimIndent()
    }

    fun getPythonDecryptionSnippet(keyPlaceholder: String = "KUNCI_ANDA"): String {
        return """
# Contoh Fungsi Dekripsi Payload / Token SFR Droid di Python
import base64, hashlib
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend

def decrypt_sfr_data(encrypted_str: str, secret_key: str) -> str:
    if not encrypted_str.startswith("ENC_V1:"):
        return encrypted_str
    parts = encrypted_str[7:].split(":")
    if len(parts) != 2:
        return encrypted_str

    iv = base64.b64decode(parts[0])
    cipher_bytes = base64.b64decode(parts[1])
    derived_key = hashlib.sha256(secret_key.encode('utf-8')).digest()

    cipher = Cipher(algorithms.AES(derived_key), modes.CBC(iv), backend=default_backend())
    decryptor = cipher.decryptor()
    padded = decryptor.update(cipher_bytes) + decryptor.finalize()
    pad_len = padded[-1]
    return padded[:-pad_len].decode('utf-8')
        """.trimIndent()
    }
}
