/* eslint-disable */



export class PhotoDetails {

    private filePath!:string;
    private photoDownloadUrl!:string;
    private priority!:number;
    private createdAtUTC!:number;


    constructor() {
    }

    public static makeInstanceFromSnapshot(snapshotNeedsParsing:any): PhotoDetails {

        const photoDetails:PhotoDetails = new PhotoDetails();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        photoDetails.setFilePath(parsedSnapshot.filePath);
        photoDetails.setPhotoDownloadUrl(parsedSnapshot.photoDownloadUrl);
        photoDetails.setPriority(parsedSnapshot.priority);
        photoDetails.setCreatedAtUTC(parsedSnapshot.createdAtUTC);

        return photoDetails;
    }

    static makeInstance(filePath:string, priority:number, photoDownloadUrl:string) {

        const photoDetails:PhotoDetails = new PhotoDetails();


        photoDetails.setFilePath(filePath);
        photoDetails.setPhotoDownloadUrl(photoDownloadUrl);
        photoDetails.setPriority(priority);
        photoDetails.setCreatedAtUTC(Date.now());

        return photoDetails;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        return {

            filePath: this.filePath,
            photoDownloadUrl: this.photoDownloadUrl,
            priority: this.priority,
            createdAtUTC: this.createdAtUTC,
        };
    }


    public getFilePath(): string {
        return this.filePath;
    }

    public setFilePath(value: string) {
        this.filePath = value;
    }

    public getPhotoDownloadUrl(): string {
        return this.photoDownloadUrl;
    }

    public setPhotoDownloadUrl(value: string) {
        this.photoDownloadUrl = value;
    }

    public getPriority(): number {
        return this.priority;
    }

    public setPriority(value: number) {
        this.priority = value;
    }

    public getCreatedAtUTC(): number {
        return this.createdAtUTC;
    }

    public setCreatedAtUTC(value: number) {
        this.createdAtUTC = value;
    }


}
