/* eslint-disable */

import {BadModelFieldException} from "../exceptions/BadModelFieldException";

export class TerrainAddress {

    private id!: string;
    private addressTitle!: string;
    private address!: string;
    private sport!: string;
    private priority!:number;
    private creatorId!: string;
    private latitude!: number;
    private longitude!: number;
    private createdAtUTC!: number;


    constructor() {
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): TerrainAddress {

        const terrainAddress:TerrainAddress = new TerrainAddress();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        console.log(parsedSnapshot);
        console.log(parsedSnapshot.id);
        terrainAddress.setId(parsedSnapshot.id);
        terrainAddress.setAddressTitle(parsedSnapshot.addressTitle);
        terrainAddress.setAddress(parsedSnapshot.address);
        terrainAddress.setSport(parsedSnapshot.sport);
        terrainAddress.setPriority(parsedSnapshot.priority);
        terrainAddress.setCreatorId(parsedSnapshot.creatorId);
        terrainAddress.setLatitude(parsedSnapshot.latitude);
        terrainAddress.setLongitude(parsedSnapshot.longitude);
        terrainAddress.setCreatedAtUTC(parsedSnapshot.createdAtUTC);

        return terrainAddress;
    }

    static makeCopy(terrainAddressToCopy: TerrainAddress | null) {

        if (terrainAddressToCopy === null)
            return null;

        const terrainAddress:TerrainAddress = new TerrainAddress();


        terrainAddress.setId(terrainAddressToCopy.getId());
        terrainAddress.setAddressTitle(terrainAddressToCopy.getAddressTitle());
        terrainAddress.setAddress(terrainAddressToCopy.getAddress());
        terrainAddress.setSport(terrainAddressToCopy.getSport());
        terrainAddress.setPriority(terrainAddressToCopy.getPriority());
        terrainAddress.setCreatorId(terrainAddressToCopy.getCreatorId());
        terrainAddress.setLatitude(terrainAddressToCopy.getLatitude());
        terrainAddress.setLongitude(terrainAddressToCopy.getLongitude());
        terrainAddress.setCreatedAtUTC(terrainAddressToCopy.getCreatedAtUTC());

        return terrainAddress;
    }


    toObject() {//this is the output that goes to firestore so becareful with the changes in this class


        return {
            id: this.id,
            addressTitle: this.addressTitle,
            address: this.address,
            sport: this.sport,
            priority: this.priority,
            creatorId: this.creatorId,
            latitude: this.latitude,
            longitude: this.longitude,
            createdAtUTC: this.createdAtUTC,
            // add other fields as needed
        };
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


        let idLength:number = 60;
        if (id.length > idLength){
            throw new BadModelFieldException("id must be less than " + idLength + " letters");
        }

        this.id = id;
    }

    public getAddressTitle(): string {
        return this.addressTitle;
    }

    public setAddressTitle(addressTitle: string) {
        if (addressTitle === undefined || typeof addressTitle !== 'string') {
            throw new BadModelFieldException("address title must be a valid string");
        }

        let addressTitleMaxLength:number = 50;
        if (addressTitle.length > addressTitleMaxLength){
            throw new BadModelFieldException("address title max length must be less than " + addressTitleMaxLength + " letters");
        }

        this.addressTitle = addressTitle;
    }

    public getAddress(): string {
        return this.address;
    }

    public setAddress(address: string) {

        if (address === undefined || typeof address !== 'string') {
            throw new BadModelFieldException("address must be a valid string");
        }

        let addressMaxLength:number = 100;
        if (address.length > addressMaxLength){
            throw new BadModelFieldException("address max length must be less than " + addressMaxLength + " letters");
        }

        this.address = address;
    }

    public getSport(): string {
        return this.sport;
    }

    public setSport(sport: string) {

        if (sport === undefined || typeof sport !== 'string') {
            throw new BadModelFieldException("sport must be a valid string");
        }

        let sportMaxLength:number = 50;
        if (sport.length > sportMaxLength){
            throw new BadModelFieldException("sport max length must be less than " + sportMaxLength + " letters");
        }

        this.sport = sport;
    }

    public getPriority(): number {
        return this.priority;
    }

    public setPriority(priority: number) {

        if (priority === undefined || typeof priority !== 'number' || isNaN(priority)) {
            throw new BadModelFieldException("latitude must be a valid number");
        }

        const min:number = -2;
        if (priority <= min){
            throw new BadModelFieldException("priority must be more than " + min + "");
        }

        this.priority = priority;
    }

    public getCreatorId(): string {
        return this.creatorId;
    }

    public setCreatorId(creatorId: string) {

        if (creatorId === undefined || typeof creatorId !== 'string') {
            throw new BadModelFieldException("creator id must be a string");
        }

        let creatorIdLength:number = 60;
        if (creatorId.length > creatorIdLength){
            throw new BadModelFieldException("creator id must be less than " + creatorIdLength + " letters");
        }

        this.creatorId = creatorId;
    }

    public getLatitude(): number {
        return this.latitude;
    }

    public setLatitude(latitude: number) {

        if (latitude === undefined || typeof latitude !== 'number' || isNaN(latitude)) {
            throw new BadModelFieldException("latitude must be a valid number");
        }

        if (latitude < -90 || latitude > 90) {
            throw new BadModelFieldException("Invalid latitude. Latitude must be between -90 and 90 degrees.");
        }

        this.latitude = latitude;
    }

    public getLongitude(): number {
        return this.longitude;
    }

    public setLongitude(longitude: number) {

        if (longitude === undefined || typeof longitude !== 'number' || isNaN(longitude)) {
            throw new BadModelFieldException("longitude must be a valid number");
        }

        if (longitude < -180 || longitude > 180) {
            throw new BadModelFieldException("Invalid longitude. Longitude must be between -180 and 180 degrees.");
        }

        this.longitude = longitude;
    }

    public getCreatedAtUTC(): number {
        return this.createdAtUTC;
    }

    public setCreatedAtUTC(createdAtUTC: number) {

        if (createdAtUTC === undefined || typeof createdAtUTC !== 'number' || isNaN(createdAtUTC)) {
            throw new BadModelFieldException("createdAtUTC must be a valid number");
        }

        let minCreatedAtUTC:number = 0;
        if (createdAtUTC < minCreatedAtUTC){
            throw new BadModelFieldException("createdAtUTC shouldn't be less than " + minCreatedAtUTC);
        }

        this.createdAtUTC = createdAtUTC;
    }



}
