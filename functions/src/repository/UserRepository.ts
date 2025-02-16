/* eslint-disable */



import {UserFirebaseDao} from "../firebasedao/UserFirebaseDao";
import {User} from "../models/user/User";
import {UserPrivacyFirebaseDao} from "../firebasedao/UserPrivacyFirebaseDao";
import {DocumentSnapshot} from "firebase-admin/lib/firestore";
import {ChatRepository} from "./ChatRepository";
import {Chat} from "../models/chat/Chat";

export class UserRepository {

    private userFirebaseDao:UserFirebaseDao;
    private userPrivacyFirebaseDao:UserPrivacyFirebaseDao;
    private chatRepository:ChatRepository;

    constructor() {
        this.userFirebaseDao = new UserFirebaseDao();
        this.userPrivacyFirebaseDao = new UserPrivacyFirebaseDao();
        this.chatRepository = new ChatRepository();
    }

    public async getUserByIdPromise(userId: string, userIdFromContext:string, keepPhoneNumber: boolean): Promise<User | null> {

        const snapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.userFirebaseDao.getUserByIdPromise(userId);
        if (snapshot == null || !snapshot.exists)
            return null;

        const user:User = User.makeInstanceFromSnapshot(snapshot.data());
        user.setEmailOrPhoneAsUsername("example@example.com");

        if (user.getUserId() !== userIdFromContext){
            user.setLatitude(0);
            user.setLongitude(0);
        }

        if (!keepPhoneNumber) {
            user.setPhoneNumber("");
            user.setPhoneCountryCode("+30");
        }

        return user;
    }


    async getUserByIdWithPhoneIfEnabled(id: string, userIdFromContext:string, chatId: string): Promise<User | null> {
        //this is for security
        const chat:Chat | undefined = await this.chatRepository.getChatById(chatId);
        if (chat === undefined || !chat.isMember(userIdFromContext) || chat.isPrivateConversation())
            return null;


        const user: User | null = await this.getUserByIdPromise(id, userIdFromContext, true);
        if (user === null)
            return null;

        const privacyDoc:DocumentSnapshot = await this.userPrivacyFirebaseDao.getPhoneNumberPermissionToThatChat(id, chatId);

        if (privacyDoc === null || privacyDoc === undefined || !privacyDoc.exists){
            user.setPhoneNumber("");

            return user;
        }

        const permission:boolean = Boolean(privacyDoc.get("permission"));
        console.log(permission);

        if (!permission){
            user.setPhoneNumber("");
            user.setPhoneCountryCode("+30");
            return user;
        }

        console.log("user",user);

        return user;
    }

    public async saveUserPromise(user: User): Promise<FirebaseFirestore.WriteResult> {
        user.setCreatedAtUTC(Date.now());

        const userFromDb:User | null = await this.getUserByIdPromise(user.getUserId(),user.getUserId(),true);
        if (userFromDb !== null)
            throw new Error("User exists");


        return this.userFirebaseDao.saveUserPromise(user);
    }

    public async updateUserPromise(user: User) {

        const userFromDb:User | null = await this.getUserByIdPromise(user.getUserId(),user.getUserId(),true);
        if (userFromDb === null)
            throw new Error("User does not exist");

        if (user.getPhoneNumber() === null || user.getPhoneNumber() === "" || user.getPhoneNumber().length !== 10){//if we change in the client sdk the country code we need to change that

            user.setPhoneNumber(userFromDb.getPhoneNumber());
            user.setPhoneCountryCode(userFromDb.getPhoneCountryCode());
        }

        return this.userFirebaseDao.updateUserPromise(user);
    }

    public saveUserProfileImagePromise(bucketFile:string,image:Buffer): Promise<void> {
        return this.userFirebaseDao.saveUserProfileImagePromise(bucketFile,image);

    }

    public deleteUserProfileImagePromise(imageUrl: string) {
        return this.userFirebaseDao.deleteUserProfileImagePromise(imageUrl);
    }


    deleteUserById(userId: string) {
        return this.userFirebaseDao.deleteUserById(userId);
    }
}
