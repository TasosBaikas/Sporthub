/* eslint-disable */


import * as admin from "firebase-admin";
import {User} from "../models/user/User";


export class UserFirebaseDao {

    private static readonly PROFILE_IMAGE_PATH:string = "images/profile_image/";

    public getUserByIdTransaction(transaction: FirebaseFirestore.Transaction, userId: string) {

        const doc = admin.firestore().doc(`users/${userId}`);

        return transaction.get(doc);
    }

    public getUserByIdPromise(userId: string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {
        return admin.firestore().doc(`users/${userId}`).get();
    }

    public saveUserPromise(user: User): Promise<FirebaseFirestore.WriteResult> {
        return admin.firestore().collection("users").doc(user.getUserId()).set(user.toObject());
    }

    public updateUserPromise(user: User) {
        return this.saveUserPromise(user);
    }

    public saveUserProfileImagePromise(bucketFile:string,imageBuffer:any): Promise<void> {

        // Set the file path and name
        const bucket = admin.storage().bucket();
        console.log("bucker",bucket);
        const file = bucket.file(UserFirebaseDao.PROFILE_IMAGE_PATH + bucketFile);
        console.log("file",file);

        // Save the image
        return file.save(imageBuffer, {
            metadata: { contentType: 'image/webp' } // Adjust the content type as needed
        });

    }

    public async deleteUserProfileImagePromise(imagePath: string): Promise<void>{
        const bucket = admin.storage().bucket(); // Default storage bucket
        const file = bucket.file(UserFirebaseDao.PROFILE_IMAGE_PATH + imagePath); // Reference to the file in the storage

        await file.delete();
    }


    deleteUserById(userId: string) {
        const doc =  admin.firestore().collection("users").doc(userId);

        return doc.delete();
    }
}
