/* eslint-disable */

import {TerrainAddress} from "./TerrainAddress";
import {BadModelFieldException} from "../exceptions/BadModelFieldException";
import {HasTerrainTypes} from "./constants/HasTerrainType";
import {SportConstants} from "./constants/SportConstants";
import {UserShortForm} from "./user/UserShortForm";
import {MatchDuration} from "./constants/MatchDuration";

export class Match {

    private id!: string;
    private sport!: string;
    private admin!: UserShortForm;
    private latitude: number = -1;
    private longitude: number = -1;
    private randomizedLocation: boolean = false;
    private inTemporalMatch!: boolean;
    private createdAtUTC!: number;
    private matchDetailsFromAdmin!: string;
    private matchDateInUTC!: number;
    private matchDuration!: number;
    private hasTerrainType!: string;
    private terrainAddress!: TerrainAddress | null;
    private levels!: number[];
    private userRequestsToJoinMatch!: string[];
    private adminIgnoredRequesters!: string[];
    private usersInChat!: string[];
    private lastModifiedTimeInUTC!: number;

    public static MIN_LEVEL:number = 1;
    public static MAX_LEVEL:number = 6;

constructor() {
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any):Match {

        const match:Match = new Match();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        match.setId(parsedSnapshot.id);
        match.setSport(parsedSnapshot.sport);
        match.setAdmin(UserShortForm.makeInstanceFromSnapshot(parsedSnapshot.admin));
        match.setRandomizedLocation(parsedSnapshot.randomizedLocation);
        match.setLatitude(parsedSnapshot.latitude);
        match.setLongitude(parsedSnapshot.longitude);
        match.setInTemporalMatch(parsedSnapshot.inTemporalMatch);

        match.setCreatedAtUTC(parsedSnapshot.createdAtUTC);

        match.setMatchDetailsFromAdmin(parsedSnapshot.matchDetailsFromAdmin);
        match.setMatchDateInUTC(parsedSnapshot.matchDateInUTC);
        match.setMatchDuration(parsedSnapshot.matchDuration);
        match.setHasTerrainType(parsedSnapshot.hasTerrainType);


        let terrainAddress:TerrainAddress | null;
        if (parsedSnapshot.terrainAddress === undefined || parsedSnapshot.terrainAddress === null)
            terrainAddress = null;
        else
            terrainAddress = TerrainAddress.makeInstanceFromSnapshot(parsedSnapshot.terrainAddress);


        match.setTerrainAddress(terrainAddress)
        match.setLevels(parsedSnapshot.levels);
        match.setUserRequestsToJoinMatch(parsedSnapshot.userRequestsToJoinMatch);
        match.setAdminIgnoredRequesters(parsedSnapshot.adminIgnoredRequesters);
        match.setUsersInChat(parsedSnapshot.usersInChat);
        match.setLastModifiedTimeInUTC(parsedSnapshot.lastModifiedTimeInUTC);


        return match;
    }

    static makeCopy(matchToCopy: Match) {

        const match:Match = new Match();



        match.setId(matchToCopy.getId());
        match.setSport(matchToCopy.getSport());
        match.setAdmin(UserShortForm.makeCopy(matchToCopy.getAdmin()));
        match.setRandomizedLocation(matchToCopy.getRandomizedLocation());
        match.setLatitude(matchToCopy.getLatitude());
        match.setLongitude(matchToCopy.getLongitude());
        match.setInTemporalMatch(matchToCopy.getInTemporalMatch());

        match.setCreatedAtUTC(matchToCopy.getCreatedAtUTC());

        match.setMatchDetailsFromAdmin(matchToCopy.getMatchDetailsFromAdmin());
        match.setMatchDateInUTC(matchToCopy.getMatchDateInUTC());
        match.setMatchDuration(matchToCopy.getMatchDuration());
        match.setHasTerrainType(matchToCopy.getHasTerrainType());


        let terrainAddress:TerrainAddress | null;
        if (matchToCopy.getTerrainAddress() === undefined || matchToCopy.getTerrainAddress() === null)
            terrainAddress = null;
        else
            terrainAddress = TerrainAddress.makeCopy(matchToCopy.getTerrainAddress());


        match.setTerrainAddress(terrainAddress)
        match.setLevels(matchToCopy.getLevels());
        match.setUserRequestsToJoinMatch(matchToCopy.getUserRequestsToJoinMatch());
        match.setAdminIgnoredRequesters(matchToCopy.getAdminIgnoredRequesters());
        match.setUsersInChat(matchToCopy.getUsersInChat());
        match.setLastModifiedTimeInUTC(matchToCopy.getLastModifiedTimeInUTC());


        return match;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        let terrainAddress:{} | null = null;
        if (this.terrainAddress !== null)
            terrainAddress = this.terrainAddress.toObject();


        return {
            id: this.id,
            sport: this.sport,
            admin: this.admin.toObject(),
            latitude: this.latitude,
            longitude: this.longitude,
            randomizedLocation: this.randomizedLocation,
            inTemporalMatch: this.inTemporalMatch,
            createdAtUTC: this.createdAtUTC,
            matchDetailsFromAdmin: this.matchDetailsFromAdmin,
            matchDateInUTC: this.matchDateInUTC,
            matchDuration: this.matchDuration,
            hasTerrainType: this.hasTerrainType,
            terrainAddress: terrainAddress,
            levels: this.levels,
            userRequestsToJoinMatch: this.userRequestsToJoinMatch,
            adminIgnoredRequesters: this.adminIgnoredRequesters,
            usersInChat: this.usersInChat,
            lastModifiedTimeInUTC: this.lastModifiedTimeInUTC,
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


        let maxLength:number = 60;
        if (id.length > maxLength){
            throw new BadModelFieldException("id must be less than " + maxLength + " letters");
        }

        this.id = id;
    }

    public getAdmin(): UserShortForm {
        return this.admin;
    }

    public setAdmin(admin: UserShortForm) {
        if (admin === undefined || !(admin instanceof UserShortForm)) {
            throw new BadModelFieldException("something is wrong with admin");
        }

        this.admin = admin;
    }

    public isAdmin(userId: string):boolean {

        return this.admin.getUserId() === userId;
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

        this.randomizeLocation(this.latitude,this.longitude);
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

        this.randomizeLocation(this.latitude,this.longitude);
    }

    private randomizeLocation(latitude: number, longitude: number): void {

        if (this.randomizedLocation)
            return;

        if (latitude === -1 || longitude === -1)
            return;

        // Maximum meters to change
        const maxDistanceMeters: number = 200;

        // Convert meters to degrees (approximately)
        const maxDegreeChange = maxDistanceMeters / 111000.0;

        // Generate random changes within the allowed range
        // Subtracting 0.5 to allow for both positive and negative changes
        const changeLat = (Math.random() - 0.5) * 2 * maxDegreeChange;
        const changeLon = (Math.random() - 0.5) * 2 * maxDegreeChange * Math.cos(latitude * Math.PI / 180);

        // Create new coordinates with these random changes
        const randomizedLatitude = latitude + changeLat;
        const randomizedLongitude = longitude + changeLon;

        this.randomizedLocation = true;

        this.latitude = randomizedLatitude;
        this.longitude = randomizedLongitude;
    }

    public getInTemporalMatch(): boolean {
        return this.inTemporalMatch;
    }

    public setInTemporalMatch(inTemporalMatch: boolean) {
        if (inTemporalMatch === undefined || typeof inTemporalMatch !== 'boolean') {
            throw new BadModelFieldException("inTemporalMatch must be a valid boolean");
        }

        this.inTemporalMatch = inTemporalMatch;
    }

    public getMatchDetailsFromAdmin(): string {
        return this.matchDetailsFromAdmin;
    }

    public setMatchDetailsFromAdmin(matchDetailsFromAdmin: string) {
        if (matchDetailsFromAdmin === undefined || matchDetailsFromAdmin === null){
            this.matchDetailsFromAdmin = "";
            return
        }

        if (matchDetailsFromAdmin === undefined || typeof matchDetailsFromAdmin !== 'string') {
            throw new BadModelFieldException("matchDetailsFromAdmin must be a string");
        }

        let matchDetailsFromAdminLength:number = 1000;
        if (matchDetailsFromAdmin.length > matchDetailsFromAdminLength){
            throw new BadModelFieldException("matchDetailsFromAdmin must be less than " + matchDetailsFromAdminLength + " letters");
        }

        this.matchDetailsFromAdmin = matchDetailsFromAdmin;
    }

    public getMatchDateInUTC(): number{
        return this.matchDateInUTC;
    }

    public setMatchDateInUTC(matchDateInUTC: number) {
        if (matchDateInUTC === undefined || typeof matchDateInUTC !== 'number' || isNaN(matchDateInUTC)) {
            throw new BadModelFieldException("createdAtUTC must be a valid number");
        }

        this.matchDateInUTC = matchDateInUTC;
    }

    public getMatchDuration(): number {
        return this.matchDuration;
    }

    public setMatchDuration(matchDuration: number) {
        if (matchDuration === undefined || typeof matchDuration !== 'number' || isNaN(matchDuration)) {
            throw new BadModelFieldException("match duration must be a number");
        }

        const minDuration:number = 0;
        if (matchDuration < minDuration)
            throw new BadModelFieldException("match duration must be higher than " + minDuration);


        if (!MatchDuration.checkDurationThatIsValidReturnBoolean(matchDuration))
            throw new BadModelFieldException("match duration is not valid");

        this.matchDuration = matchDuration;
    }

    public getHasTerrainType(): string{
        return this.hasTerrainType;
    }

    public setHasTerrainType(hasTerrainType: string) {
        if (hasTerrainType === undefined || typeof hasTerrainType !== 'string') {
            throw new BadModelFieldException("hasTerrainType must be a string");
        }

        let maxLength:number = 50;
        if (hasTerrainType.length > maxLength){
            throw new BadModelFieldException("has terrain type must be less than " + maxLength + " letters");
        }

        const found: boolean = HasTerrainTypes.TERRAIN_OPTIONS_ENGLISH_LIST.includes(hasTerrainType);
        if (!found)
            throw new BadModelFieldException("has terrain type bad value");

        this.hasTerrainType = hasTerrainType;
    }

    public getTerrainAddress(): TerrainAddress | null {
        return this.terrainAddress;
    }

    public setTerrainAddress(terrainAddress: TerrainAddress | null) {

        if (terrainAddress === null){
            this.terrainAddress = null;
            return
        }

        if (terrainAddress === undefined || !(terrainAddress instanceof TerrainAddress)) {
            throw new BadModelFieldException("something is wrong with terrainAddress");
        }


        this.terrainAddress = terrainAddress;
    }


    public getLevels(): number[] {
        return this.levels;
    }

    public setLevels(levels: number[]) {
        const minLevel = Match.MIN_LEVEL;
        const maxLevel = Match.MAX_LEVEL;
        for (const level of levels) {
            if (level === undefined || typeof level !== 'number' || isNaN(level) || !Number.isInteger(level)) {
                throw new BadModelFieldException("level must be a valid integer");
            }

            if (level < minLevel || level > maxLevel){
                throw new BadModelFieldException("level out of bounds");
            }

        }

        let deepCopy = JSON.parse(JSON.stringify(levels));


        this.levels = deepCopy;
    }

    public getUserRequestsToJoinMatch(): string[] {
        return this.userRequestsToJoinMatch;
    }

    public setUserRequestsToJoinMatch(userRequests: string[]) {

        for (const userRequest of userRequests) {
            if (userRequest === undefined || typeof userRequest !== 'string') {
                throw new BadModelFieldException("user request id must be a valid string");
            }

            let maxLength:number = 60;
            if (userRequest.length > maxLength){
                throw new BadModelFieldException("user request id must be less than " + maxLength + " letters");
            }
        }

        let deepCopy = JSON.parse(JSON.stringify(userRequests));


        this.userRequestsToJoinMatch = deepCopy;
    }

    public getAdminIgnoredRequesters(): string[] {

        return this.adminIgnoredRequesters;
    }

    public setAdminIgnoredRequesters(ignoredRequesters: string[]) {

        for (const ignored of ignoredRequesters) {
            if (ignored === undefined || typeof ignored !== 'string') {
                throw new BadModelFieldException("ignored id must be a valid string");
            }

            let maxLength:number = 60;
            if (ignored.length > maxLength){
                throw new BadModelFieldException("ignored must be less than " + maxLength + " letters");
            }
        }

        let deepCopy = JSON.parse(JSON.stringify(ignoredRequesters));

        this.adminIgnoredRequesters = deepCopy;
    }


    public isMember(userId:string): boolean {
        return this.getUsersInChat().includes(userId);
    }

    public getUsersInChat(): string[] {
        return this.usersInChat;
    }

    public setUsersInChat(usersInChat: string[]) {

        for (const user of usersInChat) {
            if (user === undefined || typeof user !== 'string') {
                throw new BadModelFieldException("user id must be a valid string");
            }

            let maxLength:number = 60;
            if (user.length > maxLength){
                throw new BadModelFieldException("user id must be less than " + maxLength + " letters");
            }
        }

        let deepCopy = JSON.parse(JSON.stringify(usersInChat));

        this.usersInChat = deepCopy;
    }

    public getLastModifiedTimeInUTC(): number {
        return this.lastModifiedTimeInUTC;
    }

    public setLastModifiedTimeInUTC(lastModifiedTimeInUTC: number) {
        const currentDate = new Date();

        this.lastModifiedTimeInUTC = currentDate.getTime();
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

    public getCreatedAtUTC(): number {
        return this.createdAtUTC;
    }

    public setCreatedAtUTC(createdAtUTC: number) {

        if (createdAtUTC === undefined || typeof createdAtUTC !== 'number' || isNaN(createdAtUTC)) {
            throw new BadModelFieldException("created at must be a number");
        }

        this.createdAtUTC = createdAtUTC;
    }


    public isUserRequestedToJoin(requesterId: string): boolean {
        return this.userRequestsToJoinMatch.includes(requesterId);
    }

    private getRandomizedLocation(): boolean{
        return this.randomizedLocation
    }

    private setRandomizedLocation(randomizedLocation: boolean) {
        if (randomizedLocation === undefined || typeof randomizedLocation !== 'boolean') {
            throw new BadModelFieldException("matchSave must be a valid boolean");
        }

        this.randomizedLocation = randomizedLocation;
    }


}
