/* eslint-disable */

import {BadModelFieldException} from "../exceptions/BadModelFieldException";
import {SportConstants} from "./constants/SportConstants";
import {MatchFilter} from "./MatchFilter";


export class FetchMatchesData {

    private dateBeginForPaginationUTC!: number;
    private dateLastForPaginationUTC!: number;
    private sport!: string;
    private lastVisibleDocumentMatchId!: string;
    private limit!: number;
    private matchFilter!:MatchFilter;


    constructor() {
    }

    public static makeInstanceFromSnapshot(snapshotNeedsParsing: any): FetchMatchesData{

        const fetchMatchesData:FetchMatchesData = new FetchMatchesData();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        fetchMatchesData.setDateBeginForPaginationUTC(parsedSnapshot.dateBeginForPaginationUTC);
        fetchMatchesData.setDateLastForPaginationUTC(parsedSnapshot.dateLastForPaginationUTC);
        fetchMatchesData.setSport(parsedSnapshot.sport);
        fetchMatchesData.setLastVisibleDocumentMatchId(parsedSnapshot.lastVisibleDocumentMatchId);
        fetchMatchesData.setLimit(parsedSnapshot.limit);

        fetchMatchesData.setMatchFilter(parsedSnapshot.matchFilter);


        return fetchMatchesData;
    }



    public getDateBeginForPaginationUTC(): number {
        return this.dateBeginForPaginationUTC;
    }

    public setDateBeginForPaginationUTC(dateBeginForPaginationUTC: number) {

        if (dateBeginForPaginationUTC === undefined || typeof dateBeginForPaginationUTC !== 'number' || isNaN(dateBeginForPaginationUTC)) {
            throw new BadModelFieldException("created at must be a valid number");
        }

        this.dateBeginForPaginationUTC = dateBeginForPaginationUTC;
    }

    public getDateLastForPaginationUTC(): number {
        return this.dateLastForPaginationUTC;
    }

    public setDateLastForPaginationUTC(dateLastForPaginationUTC: number) {

        if (dateLastForPaginationUTC === undefined || typeof dateLastForPaginationUTC !== 'number' || isNaN(dateLastForPaginationUTC)) {
            throw new BadModelFieldException("created at must be a valid number");
        }

        this.dateLastForPaginationUTC = dateLastForPaginationUTC;
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

    public getLastVisibleDocumentMatchId(): string {
        return this.lastVisibleDocumentMatchId;
    }

    public setLastVisibleDocumentMatchId(lastVisibleDocumentMatchId: string) {

        if (lastVisibleDocumentMatchId === undefined || lastVisibleDocumentMatchId === null){
            this.lastVisibleDocumentMatchId = "";
            return;
        }

        if (lastVisibleDocumentMatchId === undefined || typeof lastVisibleDocumentMatchId !== 'string') {
            throw new BadModelFieldException("lastVisibleDocumentMatchId must be a string");
        }

        let idLength:number = 60;
        if (lastVisibleDocumentMatchId.length > idLength){
            throw new BadModelFieldException("lastVisibleDocumentMatchId must be less than " + idLength + " letters");
        }

        this.lastVisibleDocumentMatchId = lastVisibleDocumentMatchId;
    }

    public getLimit(): number {
        return this.limit;
    }

    public setLimit(limit: number) {

        if (limit === undefined || typeof limit !== 'number' || isNaN(limit) || !Number.isInteger(limit)) {
            throw new BadModelFieldException("created at must be a number");
        }

        this.limit = limit;
    }


    public getMatchFilter(): MatchFilter {
        return this.matchFilter;
    }

    public setMatchFilter(parsedObject: any) {

        this.matchFilter = MatchFilter.makeInstance(parsedObject);
    }
}
