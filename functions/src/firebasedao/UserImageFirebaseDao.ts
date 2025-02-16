/* eslint-disable */


import * as admin from "firebase-admin";
import {UserImages} from "../models/user/UserImages";


export class UserImageFirebaseDao {

    public static readonly USERS_IMAGES_PATH:string = "images/users_images/";

    public saveImageToStoragePromise(filePath:string,imageBuffer:any): Promise<void> {

        // Set the file path and name
        const bucket = admin.storage().bucket();
        console.log("bucker",bucket);
        const file = bucket.file(UserImageFirebaseDao.USERS_IMAGES_PATH + filePath);
        console.log("file",file);

        // Save the image
        return file.save(imageBuffer, {
            metadata: { contentType: 'image/webp' } // Adjust the content type as needed
        });

    }

    public getUserImagePromise(userId: string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {
        return admin.firestore().collection("userImages").doc(userId).get();
    }

    // public async deleteUserProfileImagePromise(imagePath: string): Promise<void>{
    //     const bucket = admin.storage().bucket(); // Default storage bucket
    //     const file = bucket.file(UserFirebaseDao.PROFILE_IMAGE_PATH + imagePath); // Reference to the file in the storage
    //
    //     await file.delete();
    // }

    public saveUserImageInstance(userImages: UserImages): Promise<FirebaseFirestore.WriteResult> {
        return admin.firestore().collection("userImages").doc(userImages.getUserId()).set(userImages.toObject());
    }

    public async deleteImageFromStorageProfile(filePath: string): Promise<void> {
        const bucket = admin.storage().bucket(); // Default storage bucket
        const file = bucket.file(UserImageFirebaseDao.USERS_IMAGES_PATH + filePath); // Reference to the file in the storage

        await file.delete();
    }
}
