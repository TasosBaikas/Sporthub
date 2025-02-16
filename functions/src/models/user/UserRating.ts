/* eslint-disable */

import {BadModelFieldException} from "../../exceptions/BadModelFieldException";

export class UserRating {

    private userThatIsRating!:string;
    private rate!:number;
    private createdAtUTC!:number;

    constructor() {
    }

    static makeInstance(userThatIsRating:string, rate:number, createdAtUTC:number): UserRating {
        const userRating: UserRating = new UserRating();


        userRating.setUserThatIsRating(userThatIsRating);
        userRating.setRate(rate);
        userRating.setCreatedAtUTC(createdAtUTC);

        return userRating;
    }


    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): UserRating {
        const userRating: UserRating = new UserRating();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        userRating.setUserThatIsRating(parsedSnapshot.userThatIsRating);
        userRating.setRate(parsedSnapshot.rate);
        userRating.setCreatedAtUTC(parsedSnapshot.createdAtUTC);

        return userRating;
    }


    static makeCopy(userRatingToCopy: UserRating):UserRating {

        const userRating: UserRating = new UserRating();

        userRating.setUserThatIsRating(userRatingToCopy.getUserThatIsRating());
        userRating.setRate(userRatingToCopy.getRate());
        userRating.setCreatedAtUTC(userRatingToCopy.getCreatedAtUTC());

        return userRating;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        return {
            userThatIsRating: this.userThatIsRating,
            rate: this.rate,
            createdAtUTC: this.createdAtUTC,
        };
    }


    public getUserThatIsRating(): string {
        return this.userThatIsRating;
    }

    public setUserThatIsRating(userThatIsRating: string) {

        if (userThatIsRating === undefined || typeof userThatIsRating !== 'string') {
            throw new BadModelFieldException("userThatIsRating must be a string");
        }

        userThatIsRating = userThatIsRating.trim();
        if (userThatIsRating.length == 0)
            throw new BadModelFieldException("userThatIsRating length is 0");


        let maxLength:number = 60;
        if (userThatIsRating.length > maxLength){
            throw new BadModelFieldException("userThatIsRating must be less than " + maxLength + " letters");
        }

        this.userThatIsRating = userThatIsRating;
    }


    public getRate(): number {
        return this.rate;
    }

    public setRate(rate: number) {

        if (rate === undefined || typeof rate !== 'number' || isNaN(rate) || !Number.isInteger(rate)) {
            throw new BadModelFieldException("rate must be a valid integer");
        }

        let min:number = -1;
        if (rate < min){
            throw new BadModelFieldException("rate shouldn't be less than " + min);
        }

        let max:number = 5;
        if (rate > max){
            throw new BadModelFieldException("rate shouldn't be greater than " + max);
        }

        this.rate = rate;
    }

    public getCreatedAtUTC(): number {
        return this.createdAtUTC;
    }

    // Setter method for createdAtUTC
    public setCreatedAtUTC(createdAtUTC: number) {
        if (createdAtUTC === undefined || typeof createdAtUTC !== 'number' || isNaN(createdAtUTC)) {
            throw new BadModelFieldException("created at must be a valid number");
        }

        this.createdAtUTC = createdAtUTC;
    }



}
