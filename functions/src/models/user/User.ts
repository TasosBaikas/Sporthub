/* eslint-disable */

import { BadModelFieldException } from "../../exceptions/BadModelFieldException";
import {UserLevelBasedOnSport} from "./UserLevelBasedOnSport";
import { PhoneNumberUtil } from 'google-libphonenumber';
import {SportConstants} from "../constants/SportConstants";

export class User {

    private userId!: string;
    private emailOrPhoneAsUsername!:string;
    private phoneCountryCode!:string;
    private phoneNumber!:string;
    private firstName!: string;
    private lastName!: string;
    private age!: number;
    private profileImageUrl!: string;
    private profileImagePath!: string;
    private region!: string;
    private latitude!: number;
    private longitude!: number;
    private radiusSearchInM!: number;
    private userLevelBasedOnSport!: Map<string, UserLevelBasedOnSport>;
    private verificationCompleted!: boolean;
    private createdAtUTC!: number;

    private instagramLink!: string;
    private instagramUsername!: string;
    private facebookLink!: string;
    private facebookUsername!: string;

    public static MAX_RADIUS_SEARCH_IN_M:number = 25000;
    public static MIN_RADIUS_SEARCH_IN_M:number = 1500;


    constructor() {
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): User {

        const user:User = new User();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }


        user.setUserId(parsedSnapshot.userId);
        user.setEmailOrPhoneAsUsername(parsedSnapshot.emailOrPhoneAsUsername);
        user.setPhoneCountryCode(parsedSnapshot.phoneCountryCode);
        user.setPhoneNumber(parsedSnapshot.phoneNumber);
        user.setUserId(parsedSnapshot.userId);
        user.setUserId(parsedSnapshot.userId);
        user.setFirstName(parsedSnapshot.firstName);
        user.setLastName(parsedSnapshot.lastName);
        user.setAge(parsedSnapshot.age);
        user.setProfileImageUrl(parsedSnapshot.profileImageUrl);
        user.setProfileImagePath(parsedSnapshot.profileImagePath);
        user.setRegion(parsedSnapshot.region);

        user.setInstagramLink(parsedSnapshot.instagramLink);
        user.setInstagramUsername(parsedSnapshot.instagramUsername);
        user.setFacebookLink(parsedSnapshot.facebookLink);
        user.setFacebookUsername(parsedSnapshot.facebookUsername);

        user.setLatitude(parsedSnapshot.latitude);
        user.setLongitude(parsedSnapshot.longitude);
        user.setRadiusSearchInM(parsedSnapshot.radiusSearchInM);

        let userLevelParsedSnapshot;
        try{
            userLevelParsedSnapshot = JSON.parse(parsedSnapshot.userLevelBasedOnSport);
        }catch (Error){
            userLevelParsedSnapshot = parsedSnapshot.userLevelBasedOnSport;
        }

        console.log(userLevelParsedSnapshot);
        // Assuming notificationsBasedOnChats is an object with key-value pairs
        const userLevelBasedOnSport: Map<string, UserLevelBasedOnSport> = new Map<string, UserLevelBasedOnSport>();

        for (let key in userLevelParsedSnapshot) {

            if (userLevelParsedSnapshot.hasOwnProperty(key)) {
                // Use the value from parsedSnapshot to create a new UserLevelBasedOnSport instance
                const userLevel = UserLevelBasedOnSport.makeInstanceFromSnapshot(userLevelParsedSnapshot[key]);

                userLevelBasedOnSport.set(key, userLevel);
            }
        }


        user.setUserLevelBasedOnSport(userLevelBasedOnSport);

        user.setVerificationCompleted(parsedSnapshot.verificationCompleted);
        user.setCreatedAtUTC(parsedSnapshot.createdAtUTC);

        return user;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        const object: Record<string, {}> = {};
        this.userLevelBasedOnSport.forEach((value, key) => {
            object[key] = value.toObject();
        });


        return {

            userId: this.userId,
            emailOrPhoneAsUsername: this.emailOrPhoneAsUsername,
            phoneCountryCode: this.phoneCountryCode,
            phoneNumber: this.phoneNumber,
            firstName: this.firstName,
            lastName: this.lastName,
            age: this.age,
            profileImageUrl: this.profileImageUrl,
            profileImagePath: this.profileImagePath,
            region: this.region,
            instagramLink: this.instagramLink,
            instagramUsername: this.instagramUsername,
            facebookLink: this.facebookLink,
            facebookUsername: this.facebookUsername,
            latitude: this.latitude,
            longitude: this.longitude,
            radiusSearchInM: this.radiusSearchInM,
            userLevelBasedOnSport: object,
            verificationCompleted: this.verificationCompleted,
            createdAtUTC: this.createdAtUTC,
            // add other fields as needed
        };
    }

    public equals(other: User): boolean {

        return this.userId === other.userId
            && this.emailOrPhoneAsUsername === other.emailOrPhoneAsUsername
            && this.phoneCountryCode === other.phoneCountryCode
            && this.phoneNumber === other.phoneNumber
            && this.firstName === other.firstName
            && this.lastName === other.lastName
            && this.age === other.age
            && this.profileImageUrl === other.profileImageUrl
            && this.profileImagePath === other.profileImagePath
            && this.region === other.region
            && this.instagramLink === other.instagramLink
            && this.instagramUsername === other.instagramUsername
            && this.facebookLink === other.facebookLink
            && this.facebookUsername === other.facebookUsername
            && this.latitude === other.latitude
            && this.longitude === other.longitude
            && this.radiusSearchInM === other.radiusSearchInM
            && this.compareMaps(this.userLevelBasedOnSport, other.userLevelBasedOnSport)
            && this.verificationCompleted === other.verificationCompleted
            && this.createdAtUTC === other.createdAtUTC;
    }

    private compareMaps(map1: Map<string, UserLevelBasedOnSport>, map2: Map<string, UserLevelBasedOnSport>): boolean {
        if (map1.size !== map2.size) return false;

        for (let [key, userLevel1] of map1) {
            const userLevel2 = map2.get(key);
            // Check for identical objects by reference or value (deep equality for UserLevelBasedOnSport objects)
            if (userLevel2 === undefined || !userLevel2.equals(userLevel1)) return false;
        }
        return true;
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



    public getEmailOrPhoneAsUsername(): string {
        return this.emailOrPhoneAsUsername;
    }

    public setEmailOrPhoneAsUsername(emailOrPhoneAsUsername: string) {
        if (emailOrPhoneAsUsername === undefined || typeof emailOrPhoneAsUsername !== 'string') {
            throw new BadModelFieldException("userId must be a string");
        }

        emailOrPhoneAsUsername = emailOrPhoneAsUsername.trim();
        if (emailOrPhoneAsUsername.length == 0)
            throw new BadModelFieldException("emailOrPhoneAsUsername length is 0");

        let maxLength:number = 80;
        if (emailOrPhoneAsUsername.length > maxLength){
            throw new BadModelFieldException("emailOrPhoneAsUsername must be less than " + maxLength + " letters");
        }

        this.emailOrPhoneAsUsername = emailOrPhoneAsUsername;
    }

    public getPhoneCountryCode(): string {
        return this.phoneCountryCode;
    }

    public setPhoneCountryCode(phoneCountryCode: string) {
        if (phoneCountryCode === undefined || typeof phoneCountryCode !== 'string') {
            throw new BadModelFieldException("phoneCountryCode must be a string");
        }

        phoneCountryCode = phoneCountryCode.trim();
        if (phoneCountryCode.length == 0)
            throw new BadModelFieldException("phoneCountryCode length is 0");

        let maxLength:number = 4;
        if (phoneCountryCode.length > maxLength){
            throw new BadModelFieldException("phoneCountryCode must be less than " + maxLength + " letters");
        }

        this.phoneCountryCode = phoneCountryCode;
    }


    public getPhoneNumber(): string {
        return this.phoneNumber;
    }

    public setPhoneNumber(phoneNumber: string) {
        if (phoneNumber === undefined || typeof phoneNumber !== 'string') {
            throw new BadModelFieldException("phoneNumber must be a string");
        }

        if (phoneNumber === ""){
            this.phoneNumber = "";
            return
        }

        phoneNumber = phoneNumber.trim();
        if (phoneNumber.length == 0)
            throw new BadModelFieldException("phoneNumber length is 0");

        let maxLength:number = 10;
        if (phoneNumber.length > maxLength){
            throw new BadModelFieldException("phoneNumber must be less than " + maxLength + " letters");
        }

        const phoneNumberUtil = PhoneNumberUtil.getInstance();

        const parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, 'GR'); // 'GR' represents Greece
        const isValid = phoneNumberUtil.isValidNumber(parsedPhoneNumber);

        if (!isValid)
            throw new BadModelFieldException("phoneNumber has not appropriate format");


        this.phoneNumber = phoneNumber;
    }


    public getFirstName(): string {
        return this.firstName;
    }


    public setFirstName(firstName: string){
        if (firstName === undefined || typeof firstName !== 'string') {
            throw new BadModelFieldException("firstName must be a valid string");
        }

        firstName = firstName.trim();

        if (firstName.length == 0) {
            throw new BadModelFieldException("firstName must be a valid string");
        }

        let maxLength:number = 13;
        if (firstName.length > maxLength){
            throw new BadModelFieldException("first name must be less than " + maxLength + " letters");
        }

        this.firstName = firstName;
    }

    public getLastName() {
        return this.lastName;
    }


    // Setter method for lastName
    public setLastName(lastName: string) {
        if (lastName === undefined || typeof lastName !== 'string') {
            throw new BadModelFieldException("lastName must be a valid string");
        }

        lastName = lastName.trim();

        if (lastName.length == 0) {
            throw new BadModelFieldException("firstName must be a valid string");
        }

        let maxLength:number = 13;
        if (lastName.length > maxLength){
            throw new BadModelFieldException("last name must be less than " + maxLength + " letters");
        }

        this.lastName = lastName;
    }


    public getAge(): number {
        return this.age;
    }

    // Setter method for age
    public setAge(age: number) {
        if (age === undefined || typeof age !== 'number' || isNaN(age) || !Number.isInteger(age)) {
            throw new BadModelFieldException("age must be a valid integer");
        }

        let minAge:number = 15;
        if (age < minAge){
            throw new BadModelFieldException("age shouldn't be less than " + minAge);
        }

        let maxAge:number = 99;
        if (age > maxAge){
            throw new BadModelFieldException("age shouldn't be greater than " + maxAge);
        }

        this.age = age;
    }

    public getProfileImageUrl(): string {
        return this.profileImageUrl;
    }


    public setProfileImageUrl(profileImageUrl: string) {
        if (profileImageUrl === undefined || typeof profileImageUrl !== 'string') {
            throw new BadModelFieldException("profileImageUrl must be a valid string");
        }

        let profileImageUrlMaxLength:number = 400;
        if (profileImageUrl.length > profileImageUrlMaxLength){
            throw new BadModelFieldException("profile image url length must be less than " + profileImageUrlMaxLength + " letters");
        }

        this.profileImageUrl = profileImageUrl;
    }


    public getProfileImagePath(): string {
        return this.profileImagePath;
    }

    public setProfileImagePath(profileImagePath: string) {

        if (profileImagePath === undefined || typeof profileImagePath !== 'string') {
            throw new BadModelFieldException("profileImageUrl must be a valid string");
        }

        let maxLength:number = 200;
        if (profileImagePath.length > maxLength){
            throw new BadModelFieldException("profile image url length must be less than " + maxLength + " letters");
        }

        this.profileImagePath = profileImagePath;
    }

    public getRegion(): string {
        return this.region;
    }

    // Setter method for region
    public setRegion(region: string) {
        if (region === undefined || typeof region !== 'string') {
            throw new BadModelFieldException("region must be a valid string");
        }

        let regionMaxLength:number = 200;
        if (region.length > regionMaxLength){
            throw new BadModelFieldException("region max length must be less than " + regionMaxLength + " letters");
        }


        this.region = region;
    }

    public getInstagramLink(): string {
        return this.instagramLink;
    }

    public setInstagramLink(instagramLink: string) {
        if (instagramLink === undefined || typeof instagramLink !== 'string') {
            throw new BadModelFieldException("instagramLink must be a valid string");
        }

        let maxLength:number = 300;
        if (instagramLink.length > maxLength){
            throw new BadModelFieldException("instagramLink max length must be less than " + maxLength + " letters");
        }


        this.instagramLink = instagramLink;
    }


    public getInstagramUsername(): string {
        return this.instagramUsername;
    }

    public setInstagramUsername(instagramUsername: string) {
        if (instagramUsername === undefined || typeof instagramUsername !== 'string') {
            throw new BadModelFieldException("instagramUsername must be a valid string");
        }

        let maxLength:number = 80;
        if (instagramUsername.length > maxLength){
            throw new BadModelFieldException("instagramUsername max length must be less than " + maxLength + " letters");
        }


        this.instagramUsername = instagramUsername;
    }

    public getFacebookLink(): string {
        return this.facebookLink;
    }

    public setFacebookLink(facebookLink: string) {
        if (facebookLink === undefined || typeof facebookLink !== 'string') {
            throw new BadModelFieldException("facebookLink must be a valid string");
        }

        let maxLength:number = 300;
        if (facebookLink.length > maxLength){
            throw new BadModelFieldException("facebookLink max length must be less than " + maxLength + " letters");
        }


        this.facebookLink = facebookLink;
    }

    public getFacebookUsername(): string {
        return this.facebookUsername;
    }

    public setFacebookUsername(facebookUsername: string) {
        if (facebookUsername === undefined || typeof facebookUsername !== 'string') {
            throw new BadModelFieldException("facebookUsername must be a valid string");
        }

        let maxLength:number = 80;
        if (facebookUsername.length > maxLength){
            throw new BadModelFieldException("facebookUsername max length must be less than " + maxLength + " letters");
        }

        this.facebookUsername = facebookUsername;
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


    public getRadiusSearchInM(): number {
        return this.radiusSearchInM;
    }

    public setRadiusSearchInM(radiusSearchInM: number) {
        if (radiusSearchInM === undefined || typeof radiusSearchInM !== 'number' || isNaN(radiusSearchInM)) {
            throw new BadModelFieldException("radiusSearchInM must be a valid number");
        }

        let minRadiusSearchInM:number = User.MIN_RADIUS_SEARCH_IN_M;
        if (radiusSearchInM < minRadiusSearchInM){
            throw new BadModelFieldException("age shouldn't be less than " + minRadiusSearchInM);
        }

        let maxRadiusSearchInM:number = User.MAX_RADIUS_SEARCH_IN_M;
        if (radiusSearchInM > maxRadiusSearchInM){
            throw new BadModelFieldException("age shouldn't be greater than " + maxRadiusSearchInM);
        }

        this.radiusSearchInM = radiusSearchInM;
    }

    public getUserLevelBasedOnSport(): Map<string, UserLevelBasedOnSport> {
        return this.userLevelBasedOnSport;
    }

     // Setter method for userLevelBasedOnSport
    public setUserLevelBasedOnSport(userLevelBasedOnSport: Map<string, UserLevelBasedOnSport>) {
        if (userLevelBasedOnSport === undefined || !(userLevelBasedOnSport instanceof Map)) {
            throw new BadModelFieldException("userLevelBasedOnSport must be a valid Map");
        }

        for (const key of userLevelBasedOnSport.keys()) {
            if (key === undefined || typeof key !== 'string') {
                throw new BadModelFieldException("something is wrong with the model (userLevelBasedOnSport)");
            }

            if (!SportConstants.SPORTSLIST.includes(key))
                throw new BadModelFieldException("something is wrong with the model (userLevelBasedOnSport)");


            const userLevelBasedOnSportValue = userLevelBasedOnSport.get(key);
            if (!(userLevelBasedOnSportValue instanceof UserLevelBasedOnSport)){
                throw new BadModelFieldException("something is wrong with the model (userLevelBasedOnSport)");
            }
        }


        this.userLevelBasedOnSport = userLevelBasedOnSport;
    }


    public getVerificationCompleted(): boolean {
        return this.verificationCompleted;
    }

   // Setter method for verificationCompleted
   public setVerificationCompleted(verificationCompleted: boolean) {
        if (verificationCompleted === undefined || typeof verificationCompleted !== 'boolean') {
            throw new BadModelFieldException("verificationCompleted must be a valid boolean");
        }
        this.verificationCompleted = verificationCompleted;
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

