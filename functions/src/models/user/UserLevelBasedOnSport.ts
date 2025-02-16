/* eslint-disable */

import { BadModelFieldException } from "../../exceptions/BadModelFieldException";
import {Match} from "../Match";
import {SportConstants} from "../constants/SportConstants";


export class UserLevelBasedOnSport {

    private userId!:string;
    private sportName!:string;
    private priority!:number;
    private level!:number;

    constructor() {
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing:any): UserLevelBasedOnSport {

        const userLevelBasedOnSport:UserLevelBasedOnSport = new UserLevelBasedOnSport();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        userLevelBasedOnSport.setUserId(parsedSnapshot.userId);
        userLevelBasedOnSport.setSportName(parsedSnapshot.sportName);
        userLevelBasedOnSport.setPriority(parsedSnapshot.priority);
        userLevelBasedOnSport.setLevel(parsedSnapshot.level);

        return userLevelBasedOnSport;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class
        return {
            userId: this.userId,
            sportName: this.sportName,
            priority: this.priority,
            level: this.level,
            // add other fields as needed
        };
    }

    public equals(other: UserLevelBasedOnSport): boolean {
        return this.userId === other.userId
            && this.sportName === other.sportName
            && this.priority === other.priority
            && this.level === other.level;
    }

    public getUserId(): string {
        return this.userId;
    }

    public setUserId(userId: string) {
        if (userId === undefined || typeof userId !== 'string') {
            throw new BadModelFieldException("user id must be a string");
        }

        userId = userId.trim();
        if (userId.length == 0)
            throw new BadModelFieldException("userId length is 0");


        let idLength:number = 60;
        if (userId.length > idLength){
            throw new BadModelFieldException("user id must be less than " + idLength + " letters");
        }

        this.userId = userId;
    }

    public getSportName(): string {
        return this.sportName;
    }

    public setSportName(sportName: string) {
        if (sportName === undefined || typeof sportName !== 'string') {
            throw new BadModelFieldException("sport must be a valid string");
        }

        let maxLength:number = 50;
        if (sportName.length > maxLength){
            throw new BadModelFieldException("sport must be less than " + maxLength + " letters");
        }

        const found: boolean = SportConstants.SPORTSLIST.includes(sportName);
        if (!found)
            throw new BadModelFieldException("sport bad value");


        this.sportName = sportName;
    }

    public getPriority(): number {
        return this.priority;
    }

    public setPriority(priority: number) {

        if (priority === undefined || typeof priority !== 'number' || isNaN(priority) || !Number.isInteger(priority)) {
            throw new BadModelFieldException("priority must be a valid integer");
        }

        let minPriority:number = -1;
        if (priority < minPriority){
            throw new BadModelFieldException("priority shouldn't be less than " + minPriority);
        }

        this.priority = priority;
    }

    public getLevel(): number {
        return this.level;
    }

    public setLevel(level: number) {
        if (level === undefined || typeof level !== 'number' || isNaN(level) || !Number.isInteger(level)) {
            throw new BadModelFieldException("level must be a valid integer");
        }

        let minLevel:number = 0;
        if (level < minLevel){
            throw new BadModelFieldException("level shouldn't be less than " + minLevel);
        }

        let maxLevel:number = Match.MAX_LEVEL;
        if (level > maxLevel){
            throw new BadModelFieldException("level shouldn't be greater than " + maxLevel);
        }

        this.level = level;
    }


}
