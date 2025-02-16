/* eslint-disable */


import {BadModelFieldException} from "../../../exceptions/BadModelFieldException";

export class UserMatchAdminLimit {

    private userId!: string;
    private matchesMatchDateTime!:number[];

    public static MAX_MATCHES:number = 14;

    constructor() {}

    static makeInstanceFromValues(userId:string, matchesMatchDateTime:number[]): UserMatchAdminLimit {

        const user:UserMatchAdminLimit = new UserMatchAdminLimit();


        user.setUserId(userId);
        user.setMatchesMatchDateTime(matchesMatchDateTime);

        return user;
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): UserMatchAdminLimit {

        const user:UserMatchAdminLimit = new UserMatchAdminLimit();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        user.setUserId(parsedSnapshot.userId);
        user.setMatchesMatchDateTime(parsedSnapshot.matchesMatchDateTime);

        return user;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        return {

            userId: this.userId,
            matchesMatchDateTime: this.matchesMatchDateTime,
            // add other fields as needed
        };
    }


    public getUserId(): string {
        return this.userId;
    }

    public setUserId(userId: string) {
        if (userId === undefined || typeof userId !== 'string') {
            throw new BadModelFieldException("userId must be a string");
        }

        userId = userId.trim();
        if (userId.length == 0)
            throw new BadModelFieldException("userId length is 0");

        let maxLength:number = 60;
        if (userId.length > maxLength){
            throw new BadModelFieldException("userId must be less than " + maxLength + " letters");
        }

        this.userId = userId;
    }



    public getMatchesMatchDateTime(): number[] {
        return this.matchesMatchDateTime;
    }

    public setMatchesMatchDateTime(matchesMatchDateTime: number[]) {
        for (const matchDateTime of matchesMatchDateTime) {
            if (matchDateTime === undefined || typeof matchDateTime !== 'number') {
                throw new BadModelFieldException("Match date time must be a valid number");
            }
        }

        // Using spread operator for a shallow copy since it's sufficient for an array of numbers
        this.matchesMatchDateTime = [...matchesMatchDateTime];
    }

}

