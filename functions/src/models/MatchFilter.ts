/* eslint-disable */


import {BadModelFieldException} from "../exceptions/BadModelFieldException";

export class MatchFilter {

    private fromMembers!:number ;
    private toMembers!:number ;
    private enabled!:boolean ;
    private noTerrain!:boolean ;
    private yesTerrain!:boolean ;
    private maybeTerrain!:boolean;
    public static fromMembersConstant:number = 0;
    public static toMembersConstant:number = 15;


    constructor() {

    }

    public static makeInstance(snapshotNeedsParsing: any): MatchFilter{

        const filter:MatchFilter = new MatchFilter();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        filter.setFromMembers(parsedSnapshot.fromMembers);
        filter.setEnabled(parsedSnapshot.enabled);
        filter.setToMembers(parsedSnapshot.toMembers);
        filter.setYesTerrain(parsedSnapshot.yesTerrain);
        filter.setMaybeTerrain(parsedSnapshot.maybeTerrain);
        filter.setNoTerrain(parsedSnapshot.noTerrain);

        filter.validateFromAndToMembers();

        return filter;
    }


    public getFromMembers(): number {
        return this.fromMembers;
    }

    public setFromMembers(fromMembers: number) {
        if (fromMembers === undefined || typeof fromMembers !== 'number' || isNaN(fromMembers)) {
            throw new BadModelFieldException("fromMembers must be a valid number");
        }


        if (fromMembers < MatchFilter.fromMembersConstant)
            throw new BadModelFieldException("fromMembers must be >= 0");


        this.fromMembers = fromMembers;
    }

    public getToMembers(): number {
        return this.toMembers;
    }

    public setToMembers(toMembers: number) {
        if (toMembers > MatchFilter.toMembersConstant)
            throw new BadModelFieldException("fromMembers must be <= " + MatchFilter.toMembersConstant);


        this.toMembers = toMembers;
    }

    public isEnabled(): boolean {
        return this.enabled;
    }

    public setEnabled(enabled: boolean) {
        if (enabled === undefined || typeof enabled !== 'boolean')
            throw new BadModelFieldException("enabled must be a valid boolean");


        this.enabled = enabled;
    }

    public isNoTerrain(): boolean {
        return this.noTerrain;
    }

    public setNoTerrain(noTerrain: boolean) {
        if (noTerrain === undefined || typeof noTerrain !== 'boolean')
            throw new BadModelFieldException("noTerrain must be a valid boolean");


        this.noTerrain = noTerrain;
    }

    public isYesTerrain(): boolean {
        return this.yesTerrain;
    }

    public setYesTerrain(yesTerrain: boolean) {
        if (yesTerrain === undefined || typeof yesTerrain !== 'boolean')
            throw new BadModelFieldException("yesTerrain must be a valid boolean");


        this.yesTerrain = yesTerrain;
    }

    public isMaybeTerrain(): boolean {
        return this.maybeTerrain;
    }

    public setMaybeTerrain(maybeTerrain: boolean) {
        if (maybeTerrain === undefined || typeof maybeTerrain !== 'boolean')
            throw new BadModelFieldException("maybeTerrain must be a valid boolean");


        this.maybeTerrain = maybeTerrain;
    }

    private validateFromAndToMembers() {

        if (this.fromMembers > this.toMembers)
            throw new BadModelFieldException("fromMembers is bigger than toMembers");

    }
}
