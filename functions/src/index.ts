import * as functions from "firebase-functions";
// import * as messaging from "firebase-messaging";
// const messaging = require('firebase-messaging');
const admin = require('firebase-admin');
admin.initializeApp();

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
/* export const helloWorld = functions.https.onRequest((request, response) => {
  functions.logger.info("Hello logs!", {structuredData: true});
    console.log('Hello logs fdgfdg')



test


  response.send("Hello from Firebase!555555");
}); */
export const sendFcmMessaging = functions.database.ref('/youtube-posts/{pushId}/')
    .onCreate((snapshot, context) => {
        // Grab the current value of what was written to the Realtime Database.
        const original = snapshot.val();
        functions.logger.log('originalData', context.params.pushId, original);
        console.log(original)
        // You must return a Promise when performing asynchronous tasks inside a Functions such as
        // writing to the Firebase Realtime Database.
        // Setting an "uppercase" sibling in the Realtime Database returns a Promise.


        // Send a message to devices subscribed to the provided topic.
        const topic = 'YoutubeVideos';
        let messageBody = '';
        if (original.body != null) {
            if (original.body.length > 50) {

                messageBody = original.body.split('40')
            } else {
                messageBody = original.body
            }
        }

        try {
            const message = {
                data: {
                    videoId: original.YouTubeVideoId,
                    Thumb_Url: original.Thumb_Url,
                    title: original.title,
                    body: messageBody,
                    id: original.id,
                    uid: original.uid,
                },
                notification: {
                    title: original.title,
                    body: messageBody,
                    image: original.Thumb_Url
                },
                android: {
                    notification: {
                        imageUrl: original.Thumb_Url
                    }
                },
                apns: {
                    payload: {
                        aps: {
                            'mutable-content': 1
                        }
                    },
                    fcm_options: {
                        image: original.Thumb_Url
                    }
                },
                webpush: {
                    headers: {
                        image: original.Thumb_Url
                    }
                },
                topic: topic
            };

            admin.messaging().send(message)
                .then((response: any) => {
                    // Response is a message ID string.
                    console.log('Successfully sent message:', response);
                })
                .catch((error: any) => {
                    console.log('Error sending message:', error);
                });
        } catch (error: any) {
            const message = {
                data: {
                    videoId: original.YouTubeVideoId,
                    Thumb_Url: original.Thumb_Url,
                    title: 'فيديو جديد',
                    body: '',
                    id: original.id,
                    uid: original.uid,
                },
                notification: {
                    title: 'فيديو جديد',
                    body: '',
                    image: original.Thumb_Url
                },
                android: {
                    notification: {
                        imageUrl: original.Thumb_Url
                    }
                },
                apns: {
                    payload: {
                        aps: {
                            'mutable-content': 1
                        }
                    },
                    fcm_options: {
                        image: original.Thumb_Url
                    }
                },
                webpush: {
                    headers: {
                        image: original.Thumb_Url
                    }
                },
                topic: topic
            };

            admin.messaging().send(message)
                .then((response: any) => {
                    // Response is a message ID string.
                    console.log('Successfully sent message:', response);
                })
                .catch((error: any) => {
                    console.log('Error sending message:', error);
                });
        }


        return 'success';
    });
