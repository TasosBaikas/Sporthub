/* eslint-disable */


import {PhotoDetails} from "../PhotoDetails";

export class UserImages {

    private userId!:string;
    private photoDetails!:PhotoDetails[];
    public static MAX_IMAGES_SIZE: number = 20;
    public static MAX_IMAGES_MB: number = 2 * 1024 * 1024;

    constructor() {
    }

    public static makeInstanceFromSnapshot(snapshotNeedsParsing:any){

        const userImages:UserImages = new UserImages();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        userImages.setUserId(parsedSnapshot.userId);

        const photoDetails: PhotoDetails[] = [];
        for (const photoDetail of parsedSnapshot.photoDetails) {
            photoDetails.push(PhotoDetails.makeInstanceFromSnapshot(photoDetail));
        }
        userImages.setPhotoDetails(photoDetails);

        return userImages;

    }

    public static instanceWithDefaultValues(userId:string) {

        const userImages:UserImages = new UserImages();

        userImages.setUserId(userId);
        userImages.setPhotoDetails([]);

        return userImages;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        const photoDetailsForFirebase: {}[] = [];
        for (const photoDetail of this.photoDetails) {
            photoDetailsForFirebase.push(photoDetail.toObject());
        }

        return {
            userId: this.userId,
            photoDetails: photoDetailsForFirebase,
        };
    }


    public getUserId(): string {
        return this.userId;
    }

    public setUserId(value: string) {
        this.userId = value;
    }

    public getPhotoDetails(): PhotoDetails[] {
        return this.photoDetails;
    }

    public setPhotoDetails(value: PhotoDetails[]) {
        this.photoDetails = value;
    }
}
