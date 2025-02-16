/* eslint-disable */

import {BadModelFieldException} from "../../exceptions/BadModelFieldException";
import {UserRating} from "./UserRating";

export class UserRatingList {

    private ratedUser!:string;
    private userRatingsList!: UserRating[];

    constructor() {
    }

    static makeInstance(ratedUser:string, userRatingsList: UserRating[]): UserRatingList {
        const instance: UserRatingList = new UserRatingList();

        instance.setRatedUser(ratedUser);
        instance.setUserRatingsList(userRatingsList)

        return instance;
    }


    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): UserRatingList {
        const userRatingList: UserRatingList = new UserRatingList();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        userRatingList.setRatedUser(parsedSnapshot.ratedUser);

        const userRatingsList: UserRating[] = [];
        for (const userRating of parsedSnapshot.userRatingsList) {
            userRatingsList.push(UserRating.makeInstanceFromSnapshot(userRating));
        }
        userRatingList.setUserRatingsList(userRatingsList);


        return userRatingList;
    }



    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        const userRatingObject: {}[] = [];
        for (const userRating of this.userRatingsList) {
            userRatingObject.push(userRating.toObject());
        }


        return {
            ratedUser: this.ratedUser,
            userRatingsList: userRatingObject,
        };
    }

    public getRatedUser(): string {
        return this.ratedUser;
    }

    public setRatedUser(ratedUser: string) {

        if (ratedUser === undefined || typeof ratedUser !== 'string') {
            throw new BadModelFieldException("ratedUser must be a string");
        }

        ratedUser = ratedUser.trim();
        if (ratedUser.length == 0)
            throw new BadModelFieldException("ratedUser length is 0");


        let maxLength:number = 60;
        if (ratedUser.length > maxLength){
            throw new BadModelFieldException("ratedUser must be less than " + maxLength + " letters");
        }

        this.ratedUser = ratedUser;
    }


    public getUserRatingsList(): UserRating[] {
        return this.userRatingsList;
    }

    public setUserRatingsList(userRatingsList: UserRating[]) {

        for (const userRating of userRatingsList) {

            if (userRating === undefined || !(userRating instanceof UserRating)) {
                throw new BadModelFieldException("something is wrong with userRating");
            }

        }

        this.userRatingsList = userRatingsList;
    }


}
