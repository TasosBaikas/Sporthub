/* eslint-disable */

import {BadModelFieldException} from "../../exceptions/BadModelFieldException";
import {UserLevelBasedOnSport} from "./UserLevelBasedOnSport";
import {User} from "./User";

export class UserShortForm {

    private userId!: string;
    private firstName!: string;
    private lastName!: string;
    private age!: number;
    private profileImageUrl!: string;
    private oneSpecifiedSport! :UserLevelBasedOnSport | null;
    private region!: string;


    constructor() {
    }

    static makeInstanceFromSnapshot(snapshotNeedsParsing: any): UserShortForm {
        const userShortForm: UserShortForm = new UserShortForm();

        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(snapshotNeedsParsing);
        }catch (Error){
            parsedSnapshot = snapshotNeedsParsing;
        }

        let oneSpecifiedSport:UserLevelBasedOnSport | null = null;
        if (parsedSnapshot.oneSpecifiedSport)
            oneSpecifiedSport = UserLevelBasedOnSport.makeInstanceFromSnapshot(parsedSnapshot.oneSpecifiedSport)


        userShortForm.setUserId(parsedSnapshot.userId);
        userShortForm.setFirstName(parsedSnapshot.firstName);
        userShortForm.setLastName(parsedSnapshot.lastName);
        userShortForm.setAge(parsedSnapshot.age);
        userShortForm.setProfileImageUrl(parsedSnapshot.profileImageUrl);
        userShortForm.setOneSpecifiedSport(oneSpecifiedSport);
        userShortForm.setRegion(parsedSnapshot.region);

        return userShortForm;
    }

    static makeShortForFromUser(user: User, chosenSport:string | null): UserShortForm {
        const userShortForm: UserShortForm = new UserShortForm();


        userShortForm.setUserId(user.getUserId());
        userShortForm.setFirstName(user.getFirstName());
        userShortForm.setLastName(user.getLastName());
        userShortForm.setAge(user.getAge());
        userShortForm.setProfileImageUrl(user.getProfileImageUrl());

        let oneSpecifiedSport: UserLevelBasedOnSport | null;
        if (chosenSport === null || chosenSport === ""){
            oneSpecifiedSport = null;
        }else{
            oneSpecifiedSport = UserLevelBasedOnSport.makeInstanceFromSnapshot(user.getUserLevelBasedOnSport().get(chosenSport));
        }
        userShortForm.setOneSpecifiedSport(oneSpecifiedSport);

        userShortForm.setRegion(user.getRegion());

        return userShortForm;
    }

    static makeNullUserShortForm(id: string): UserShortForm {
        const userShortForm: UserShortForm = new UserShortForm();

        userShortForm.setUserId(id);
        userShortForm.setFirstName("Anonymous");
        userShortForm.setLastName("Anonymous");
        userShortForm.setAge(99);
        userShortForm.setProfileImageUrl("");

        userShortForm.setOneSpecifiedSport(null);

        userShortForm.setRegion("Anonymous");

        return userShortForm;
    }

    static makeDeletedUserShortForm(id: string): UserShortForm {
        const userShortForm: UserShortForm = new UserShortForm();

        userShortForm.setUserId(id);
        userShortForm.setFirstName("Διαγράφηκε");
        userShortForm.setLastName("Διαγράφηκε");
        userShortForm.setAge(99);
        userShortForm.setProfileImageUrl("");

        userShortForm.setOneSpecifiedSport(null);

        userShortForm.setRegion("Διαγράφηκε");

        return userShortForm;
    }

    static makeCopy(userShortFormToCopy: UserShortForm) {

        const userShortForm: UserShortForm = new UserShortForm();

        userShortForm.setUserId(userShortFormToCopy.getUserId());
        userShortForm.setFirstName(userShortFormToCopy.getFirstName());
        userShortForm.setLastName(userShortFormToCopy.getLastName());
        userShortForm.setAge(userShortFormToCopy.getAge());
        userShortForm.setProfileImageUrl(userShortFormToCopy.getProfileImageUrl());

        userShortForm.setOneSpecifiedSport(userShortFormToCopy.getOneSpecifiedSport());

        userShortForm.setRegion(userShortFormToCopy.getRegion());

        return userShortForm;
    }

    toObject() {//this is the output that goes to firestore so becareful with the changes in this class

        let oneSpecifiedSport:{} | null = null;
        if (this.oneSpecifiedSport !== null)
            oneSpecifiedSport = this.oneSpecifiedSport.toObject();

        return {
            userId: this.userId,
            firstName: this.firstName,
            lastName: this.lastName,
            age: this.age,
            profileImageUrl: this.profileImageUrl,
            oneSpecifiedSport: oneSpecifiedSport,
            region: this.region,
        };
    }

    public getUserId(): string {
        return this.userId;
    }

    public setUserId(userId: string) {

        if (userId === undefined || typeof userId !== 'string') {
            throw new BadModelFieldException("id must be a string");
        }

        userId = userId.trim();
        if (userId.length == 0)
            throw new BadModelFieldException("userId length is 0");


        let maxLength:number = 60;
        if (userId.length > maxLength){
            throw new BadModelFieldException("id must be less than " + maxLength + " letters");
        }

        this.userId = userId;
    }

    public getFirstName(): string {
        return this.firstName;
    }

    public setFirstName(firstName: string) {
        if (firstName === undefined || typeof firstName !== 'string') {
            throw new BadModelFieldException("firstName must be a valid string");
        }

        firstName = firstName.trim();

        if (firstName.trim().length == 0) {
            throw new BadModelFieldException("firstName must be a valid string");
        }

        let maxLength:number = 13;
        if (firstName.length > maxLength){
            throw new BadModelFieldException("first name must be less than " + maxLength + " letters");
        }

        this.firstName = firstName;
    }

    public getLastName(): string {
        return this.lastName;
    }

    public setLastName(lastName: string) {

        if (lastName === undefined || typeof lastName !== 'string') {
            throw new BadModelFieldException("lastName must be a valid string");
        }

        lastName = lastName.trim();

        if (lastName.trim().length == 0) {
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


    public getOneSpecifiedSport(): UserLevelBasedOnSport | null{
        return this.oneSpecifiedSport;
    }

    public setOneSpecifiedSport(oneSpecifiedSport: UserLevelBasedOnSport | null) {
        if (oneSpecifiedSport === null){
            this.oneSpecifiedSport = null;
            return;
        }

        if (oneSpecifiedSport === undefined || !(oneSpecifiedSport instanceof UserLevelBasedOnSport)) {
            throw new BadModelFieldException("something is wrong with chatMessage");
        }

        this.oneSpecifiedSport = oneSpecifiedSport;
    }

    public getRegion(): string {
        return this.region;
    }

    public setRegion(region: string) {

        if (region === undefined || typeof region !== 'string') {
            throw new BadModelFieldException("region must be a valid string");
        }

        let regionMaxLength:number = 100;
        if (region.length > regionMaxLength){
            throw new BadModelFieldException("region max length must be less than " + regionMaxLength + " letters");
        }


        this.region = region;
    }



}
