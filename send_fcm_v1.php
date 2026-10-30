<?php
/**
 * ==============================================================================
 * SFR DROID - PURE PHP SENDER SCRIPT UNTUK FIREBASE CLOUD MESSAGING (FCM) API V1
 * ==============================================================================
 * Cocok untuk SHARED HOSTING (cPanel, DirectAdmin, dsb.)
 * TANPA Composer, TANPA Autoload, TANPA Library Pihak Ketiga!
 *
 * Persyaratan Hosting:
 * - PHP 7.4 / 8.0 / 8.1 / 8.2 / 8.3
 * - Ekstensi OpenSSL aktif (standar di 99.9% shared hosting)
 * - Ekstensi cURL aktif
 *
 * CARA MENGGUNAKAN:
 * 1. Buka Firebase Console -> Project Settings -> Service accounts
 * 2. Klik "Generate new private key" dan unduh file JSON-nya.
 * 3. Simpan file tersebut dengan nama "service-account.json" di folder yang sama dengan script ini.
 * 4. Panggil script ini melalui browser atau CURL:
 *    http://domain-anda.com/send_fcm_v1.php?token=TOKEN_HP_PENGGUNA&title=Halo&body=Pesan+dari+SFR+Droid
 *    atau kirim ke semua user via topik:
 *    http://domain-anda.com/send_fcm_v1.php?topic=all&title=Pembaruan&body=Ada+update+terbaru!
 * ==============================================================================
 */

header('Content-Type: application/json; charset=utf-8');

// Konfigurasi Path File Service Account
$serviceAccountFile = __DIR__ . '/service-account.json';

if (!file_exists($serviceAccountFile)) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'File service-account.json tidak ditemukan di folder yang sama! Unduh dari Firebase Console -> Project Settings -> Service accounts -> Generate new private key.'
    ], JSON_PRETTY_PRINT);
    exit;
}

$serviceAccount = json_decode(file_get_contents($serviceAccountFile), true);
if (!$serviceAccount || !isset($serviceAccount['private_key'], $serviceAccount['client_email'], $serviceAccount['project_id'])) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Format file service-account.json tidak valid atau data kunci privat tidak lengkap.'
    ], JSON_PRETTY_PRINT);
    exit;
}

// Tangkap Parameter dari GET atau POST
$targetToken = $_REQUEST['token'] ?? '';
$targetTopic = $_REQUEST['topic'] ?? '';
$title = $_REQUEST['title'] ?? 'Pemberitahuan SFR Droid';
$body  = $_REQUEST['body'] ?? $_REQUEST['message'] ?? 'Notifikasi real-time dari developer.';
$linkUrl = $_REQUEST['url'] ?? '';

if (empty($targetToken) && empty($targetTopic)) {
    // Jika tidak ada target, default ke topic "all"
    $targetTopic = 'all';
}

/**
 * Fungsi Base64 URL Encoding (Sesuai spesifikasi RFC 7515)
 */
function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

/**
 * Langkah 1: Buat dan Tandatangani JWT (JSON Web Token) dengan Algoritma RS256
 */
function generateGoogleJwt($clientEmail, $privateKey) {
    $now = time();
    $header = ['alg' => 'RS256', 'typ' => 'JWT'];
    $claims = [
        'iss' => $clientEmail,
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',
        'exp' => $now + 3600,
        'iat' => $now
    ];

    $encodedHeader = base64UrlEncode(json_encode($header));
    $encodedClaims = base64UrlEncode(json_encode($claims));
    $dataToSign = $encodedHeader . '.' . $encodedClaims;

    $signature = '';
    $success = openssl_sign($dataToSign, $signature, $privateKey, OPENSSL_ALGO_SHA256);
    if (!$success) {
        throw new Exception('Gagal menandatangani JWT dengan OpenSSL. Periksa format private_key.');
    }

    return $dataToSign . '.' . base64UrlEncode($signature);
}

/**
 * Langkah 2: Tukar JWT dengan Google OAuth2 Access Token
 */
function getGoogleAccessToken($jwt) {
    $ch = curl_init('https://oauth2.googleapis.com/token');
    $postFields = http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt
    ]);

    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $postFields);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/x-www-form-urlencoded']);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($httpCode !== 200) {
        throw new Exception("Gagal mendapatkan Access Token dari Google (HTTP $httpCode): " . $response);
    }

    $json = json_decode($response, true);
    return $json['access_token'] ?? null;
}

try {
    // 1. Generate JWT & Dapatkan Access Token
    $jwt = generateGoogleJwt($serviceAccount['client_email'], $serviceAccount['private_key']);
    $accessToken = getGoogleAccessToken($jwt);

    if (!$accessToken) {
        throw new Exception('Access Token kosong.');
    }

    // 2. Susun Payload FCM HTTP v1
    $projectId = $serviceAccount['project_id'];
    $fcmEndpoint = "https://fcm.googleapis.com/v1/projects/{$projectId}/messages:send";

    $messagePayload = [
        'notification' => [
            'title' => $title,
            'body'  => $body
        ],
        'android' => [
            'priority' => 'HIGH',
            'notification' => [
                'sound' => 'sfr_droid', // Memanggil file suara sfr_droid.mp3 di res/raw
                'channel_id' => 'sfr_dev_custom_audio_v5',
                'default_vibrate_timings' => true,
                'notification_priority' => 'PRIORITY_HIGH'
            ]
        ],
        'data' => [
            'title' => $title,
            'message' => $body,
            'url' => $linkUrl,
            'timestamp' => (string)(time() * 1000)
        ]
    ];

    if (!empty($targetToken)) {
        $messagePayload['token'] = $targetToken;
    } else {
        $messagePayload['topic'] = $targetTopic;
    }

    $fcmRequestBody = json_encode(['message' => $messagePayload]);

    // 3. Kirim ke API FCM v1
    $ch = curl_init($fcmEndpoint);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $fcmRequestBody);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: Bearer ' . $accessToken,
        'Content-Type: application/json; UTF-8'
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 15);

    $fcmResponse = curl_exec($ch);
    $fcmHttpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    $fcmJson = json_decode($fcmResponse, true);

    if ($fcmHttpCode >= 200 && $fcmHttpCode < 300) {
        echo json_encode([
            'success' => true,
            'http_code' => $fcmHttpCode,
            'message' => 'Notifikasi FCM berhasil terkirim ke perangkat secara real-time!',
            'fcm_response' => $fcmJson,
            'target' => !empty($targetToken) ? 'Token: ' . substr($targetToken, 0, 15) . '...' : 'Topic: ' . $targetTopic
        ], JSON_PRETTY_PRINT);
    } else {
        http_response_code($fcmHttpCode);
        echo json_encode([
            'success' => false,
            'http_code' => $fcmHttpCode,
            'message' => 'FCM API v1 menolak pengiriman.',
            'error_detail' => $fcmJson
        ], JSON_PRETTY_PRINT);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ], JSON_PRETTY_PRINT);
}
