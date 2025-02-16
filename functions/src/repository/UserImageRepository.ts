/* eslint-disable */


import {UserImageFirebaseDao} from "../firebasedao/UserImageFirebaseDao";
import {UserImages} from "../models/user/UserImages";
const functions = require("firebase-functions");

export class UserImageRepository {

    private userImageFirebaseDao:UserImageFirebaseDao;
    private userIdFromContext:string;


    constructor(yourId:string,yourIdFromContext:string) {
        this.userImageFirebaseDao = new UserImageFirebaseDao();

        this.userIdFromContext = yourIdFromContext;

        if (yourId !==  this.userIdFromContext){
            throw new functions.https.HttpsError(
                'unauthenticated',
                'You must be authenticated to call this function.'
            );
        }
    }

    public saveImageToStoragePromise(filePath:string,imageBuffer:any): Promise<void> {
        this.validation(filePath);

        return this.userImageFirebaseDao.saveImageToStoragePromise(filePath, imageBuffer);
    }

    public async getUserImagePromise(userId:string): Promise<UserImages> {

        const snapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.userImageFirebaseDao.getUserImagePromise(userId);

        const data: FirebaseFirestore.DocumentData | undefined = snapshot.data();
        if (data === undefined){

            return UserImages.instanceWithDefaultValues(userId);
        }

        return UserImages.makeInstanceFromSnapshot(data);
    }

    public saveUserImageInstance(userImages: UserImages): Promise<FirebaseFirestore.WriteResult> {

        return this.userImageFirebaseDao.saveUserImageInstance(userImages);
    }

    public deleteImageFromStorageProfile(filePath: string) {
        this.validation(filePath);

        return this.userImageFirebaseDao.deleteImageFromStorageProfile(filePath);

    }

    public validation(filePath:string){

        const yourIdFromReference: string = filePath.substring(0,filePath.indexOf("_"));
        if (yourIdFromReference !== this.userIdFromContext){
            throw new functions.https.HttpsError(
                'unauthenticated',
                'You must be authenticated to call this function.'
            );
        }
    }

}
