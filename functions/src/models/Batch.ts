/* eslint-disable */


import {Match} from "./Match";
import {BadModelFieldException} from "../exceptions/BadModelFieldException";
import {SportConstants} from "./constants/SportConstants";

export class Batch{

    private id!: string;
    private matchesList!: Match[];
    private countMatches!: number;
    private empty!: boolean;
    private sport!: string;
    public static MAX_SIZE_BATCH:number = 100;


    constructor() {
    }

    public static makeInstance(id:string, matchesList:Match[], sport:string):Batch {

        const batch:Batch = new Batch();

        batch.setId(id);
        batch.setMatchesList(matchesList);
        batch.setSport(sport);

        batch.updateCountMatchesAndEmpty()

        return batch;
    }

    public static makeInstanceFromSnapshot(snapshotNeedsParsing: any):Batch {

        const batch:Batch = new Batch();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        batch.setId(parsedSnapshot.id);
        batch.setSport(parsedSnapshot.sport)


        const matchesList: Match[] = [];
        for (const match of parsedSnapshot.matchesList) {
            matchesList.push(Match.makeInstanceFromSnapshot(match));
        }
        batch.setMatchesList(matchesList);

        batch.setCountMatches(matchesList.length);

        if (batch.getCountMatches() === 0)
            batch.setEmpty(true);
        else
            batch.setEmpty(false);

        batch.updateCountMatchesAndEmpty()

        return batch;
    }


    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        const matchesListForFirebase: {}[] = [];
        for (const match of this.matchesList) {
            matchesListForFirebase.push(match.toObject());
        }

        return {
            id: this.id,
            matchesList: matchesListForFirebase,
            countMatches: this.countMatches,
            empty: this.empty,
            sport: this.sport,
            // add other fields as needed
        };
    }


    updateMatches(matchesToUpdate: Match[]): Match[] {

        const theLeftOnes:Match[] = [];
        matchesToUpdate.forEach(matchToUpdate => {
            // Find the index of the match with the same id as newMatch
            const matchIndex = this.matchesList.findIndex(existingMatch => existingMatch.getId() === matchToUpdate.getId());

            // If there is a match with the same id, update it
            if (matchIndex !== -1) {
                this.matchesList[matchIndex] = matchToUpdate; // This overwrites the existing match with newMatch
            }else{
                theLeftOnes.push(matchToUpdate);
            }
        });

        return theLeftOnes;
    }

    public addMatchesOrUpdateToBatch(matches: Match[]): void {
        matches.forEach(newMatch => {
            // Find the index of the match with the same id as newMatch
            const matchIndex = this.matchesList.findIndex(existingMatch => existingMatch.getId() === newMatch.getId());

            // If there is a match with the same id, update it
            if (matchIndex !== -1) {
                this.matchesList[matchIndex] = newMatch; // This overwrites the existing match with newMatch
            } else {
                // If there is no match with the same id, add newMatch to matchesList
                this.matchesList.push(newMatch);
            }
        });


        this.updateCountMatchesAndEmpty()
    }

    public removeExpiredMatchesFromBatch(): void {
        // Get the current time in epoch milliseconds
        const currentTime = new Date().getTime();

        // Filter out matches that have matchDateInUTC less than the current time
        this.matchesList = this.matchesList.filter(match => match.getMatchDateInUTC() && match.getMatchDateInUTC() >= currentTime);

        // Update countMatches to reflect the current length of matchesList
        this.updateCountMatchesAndEmpty();
    }


    public updateCountMatchesAndEmpty():void{
        this.setCountMatches(this.matchesList.length);

        if (this.getCountMatches() === 0)
            this.setEmpty(true);
        else
            this.setEmpty(false)

    }



    public getId(): string {
        return this.id;
    }

    public setId(id: string) {

        if (id === undefined || typeof id !== 'string') {
            throw new BadModelFieldException("id must be a string");
        }

        id = id.trim();
        if (id.length == 0)
            throw new BadModelFieldException("id length is 0");


        let maxLength:number = 60;
        if (id.length > maxLength){
            throw new BadModelFieldException("id must be less than " + maxLength + " letters");
        }


        this.id = id;
    }

    public getMatchesList(): Match[] {
        return this.matchesList;
    }

    public setMatchesList(matchesList: Match[]) {

        for (const match of matchesList) {

            if (match === undefined || !(match instanceof Match)) {
                throw new BadModelFieldException("something is wrong with chatMessage");
            }

        }

        this.matchesList = matchesList;
    }

    public getSport(): string {
        return this.sport;
    }

    public setSport(sport: string) {

        if (sport === undefined || typeof sport !== 'string') {
            throw new BadModelFieldException("sport must be a valid string");
        }

        let maxLength:number = 50;
        if (sport.length > maxLength){
            throw new BadModelFieldException("sport must be less than " + maxLength + " letters");
        }

        const found: boolean = SportConstants.SPORTSLIST.includes(sport);
        if (!found)
            throw new BadModelFieldException("sport bad value");


        this.sport = sport;
    }


    public getCountMatches(): number {
        return this.countMatches;
    }

    public setCountMatches(countMatches: number) {
        this.countMatches = countMatches;
    }

    public getEmpty(): boolean {
        return this.empty;
    }

    public setEmpty(empty: boolean) {
        this.empty = empty;
    }
}


