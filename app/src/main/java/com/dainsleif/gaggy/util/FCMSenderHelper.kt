package com.dainsleif.gaggy.util

/**
 * Helper class with instructions on how to send FCM messages from a server
 * 
 * This class doesn't contain actual code to run in the app, but serves as documentation
 * on how to send FCM messages to users from your server.
 */
class FCMSenderHelper {
    
    companion object {
        /**
         * To send a message to a specific topic (e.g., "announcements"), you need to make an HTTP POST request
         * to the Firebase Cloud Messaging API.
         * 
         * Here's an example using curl:
         * 
         * ```
         * curl -X POST \
         *   https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send \
         *   -H "Content-Type: application/json" \
         *   -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
         *   -d '{
         *     "message": {
         *       "topic": "announcements",
         *       "notification": {
         *         "title": "Important Announcement",
         *         "body": "This is an important announcement for all users."
         *       }
         *     }
         *   }'
         * ```
         * 
         * To get YOUR_ACCESS_TOKEN, you need to:
         * 1. Use a service account with the Firebase Admin SDK
         * 2. Generate an OAuth2 token
         * 
         * Alternatively, you can use the Firebase Admin SDK in your server code:
         * 
         * Node.js example:
         * ```javascript
         * const admin = require('firebase-admin');
         * 
         * // Initialize the app with a service account
         * admin.initializeApp({
         *   credential: admin.credential.cert('path/to/serviceAccountKey.json')
         * });
         * 
         * // Send a message to devices subscribed to the provided topic
         * function sendToTopic() {
         *   const message = {
         *     notification: {
         *       title: 'Important Announcement',
         *       body: 'This is an important announcement for all users.'
         *     },
         *     topic: 'announcements'
         *   };
         * 
         *   admin.messaging().send(message)
         *     .then((response) => {
         *       console.log('Successfully sent message:', response);
         *     })
         *     .catch((error) => {
         *       console.log('Error sending message:', error);
         *     });
         * }
         * ```
         * 
         * To send to specific devices, replace the topic with token:
         * ```javascript
         * const message = {
         *   notification: {
         *     title: 'Important Announcement',
         *     body: 'This is an important announcement for you.'
         *   },
         *   token: 'USER_FCM_TOKEN'
         * };
         * ```
         * 
         * For more information, see the Firebase Cloud Messaging documentation:
         * https://firebase.google.com/docs/cloud-messaging/send-message
         */
        const val DOCUMENTATION = "See class comments for instructions on sending FCM messages"
    }
} 