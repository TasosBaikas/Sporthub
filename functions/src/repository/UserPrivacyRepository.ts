/* eslint-disable */


import {UserPrivacyFirebaseDao} from "../firebasedao/UserPrivacyFirebaseDao";
import {UserChatAction} from "../models/userprivacy/UserChatAction";
import {DocumentSnapshot} from "firebase-admin/lib/firestore";
import {UserBlockedPlayers} from "../models/userprivacy/UserBlockedPlayers";
import {ValidationException} from "../exceptions/ValidationException";
import {UserRating} from "../models/user/UserRating";
import {UserRatingList} from "../models/user/UserRatingList";

const admin = require('firebase-admin');


export class UserPrivacyRepository {

    private userPrivacyFirebaseDao:UserPrivacyFirebaseDao;

    constructor() {
        this.userPrivacyFirebaseDao = new UserPrivacyFirebaseDao();
    }

    public async getUserActivityToAChat(userId: string): Promise<UserChatAction> {

        const userActivitySnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getUserActivityToAChat(userId);

        let userChatAction:UserChatAction;
        if (userActivitySnapshot == null || !userActivitySnapshot.exists){
            userChatAction = UserChatAction.makeInstance(userId, []);
        }else{
            userChatAction = UserChatAction.makeInstanceFromSnapshot(userActivitySnapshot.data());
        }

        return userChatAction;
    }

    deleteUserActivityToAChat(userId: string): Promise<FirebaseFirestore.WriteResult> {

        return this.userPrivacyFirebaseDao.deleteUserActivityToAChat(userId);
    }


    deletePhoneNumberPermissions(userId: string) {

        return this.userPrivacyFirebaseDao.deletePhoneNumberPermissions(userId);
    }

    async unblockUser(userToUnblock: string, yourIdFromContext: string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const blockedSnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getBlockedPlayersByUserId(yourIdFromContext);

            if (blockedSnapshot == null || !blockedSnapshot.exists)
                throw new ValidationException("Ο χρήστης είναι ήδη unblock");

            const blockedPlayers: UserBlockedPlayers = UserBlockedPlayers.makeInstanceFromSnapshot(blockedSnapshot.data());

            const blockedPlayersList:string[] = blockedPlayers.getBlockedPlayers();

            if (!blockedPlayersList.includes(userToUnblock))
                throw new ValidationException("Ο χρήστης είναι ήδη unblock");


            const indexToRemove: number = blockedPlayersList.findIndex((userId:string) => userId === userToUnblock);
            if (indexToRemove !== -1) {
                blockedPlayersList.splice(indexToRemove, 1);
            }

            await this.userPrivacyFirebaseDao.updateBlockedPlayersTransaction(transaction, blockedPlayers);

            return null;
        });

    }

    async blockUser(userIdToBlock: string, yourIdFromContext:string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const blockedSnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getBlockedPlayersByUserId(yourIdFromContext);

            let blockedPlayers: UserBlockedPlayers;
            if (blockedSnapshot == null || !blockedSnapshot.exists){

                blockedPlayers = UserBlockedPlayers.makeInstance(yourIdFromContext,[]);
            }else{
                blockedPlayers = UserBlockedPlayers.makeInstanceFromSnapshot(blockedSnapshot.data());
            }

            const blockedPlayersList:string[] = blockedPlayers.getBlockedPlayers();

            if (blockedPlayersList.includes(userIdToBlock))
                throw new ValidationException("Ο χρήστης έχει ήδη γίνει block");

            blockedPlayersList.push(userIdToBlock);


            await this.userPrivacyFirebaseDao.updateBlockedPlayersTransaction(transaction, blockedPlayers);

            return null;
        });

    }

    async rateUser(userToRate: string, rate: number, raterFromContext:string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const userRatingSnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getUserRatingTransaction(transaction, userToRate);

            let userRatingList: UserRatingList;
            if (userRatingSnapshot == null || !userRatingSnapshot.exists){

                userRatingList = UserRatingList.makeInstance(userToRate,[]);
            }else{
                userRatingList = UserRatingList.makeInstanceFromSnapshot(userRatingSnapshot.data());
            }

            const userRatingsList:UserRating[] = userRatingList.getUserRatingsList();

            const raterRateList:UserRating[] = userRatingsList
                .filter((userRating:UserRating) => userRating.getUserThatIsRating() === raterFromContext);

            if (raterRateList.length === 0){

                const raterRate: UserRating = UserRating.makeInstance(raterFromContext, rate, Date.now());

                userRatingsList.push(raterRate);

                await this.userPrivacyFirebaseDao.rateUserTransaction(transaction, userRatingList);
                return null
            }

            const raterRate: UserRating = raterRateList[0];

            raterRate.setRate(rate);
            raterRate.setCreatedAtUTC(Date.now());


            await this.userPrivacyFirebaseDao.rateUserTransaction(transaction, userRatingList);

            return null;
        });

    }

    async deleteUserRating(userOfRating: string, raterOfDeletionFromContext:string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const userOfRatingSnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getUserRatingTransaction(transaction, userOfRating);

            if (userOfRatingSnapshot == null || !userOfRatingSnapshot.exists)
                throw new ValidationException("Έχει ήδη διαγραφεί");


            const userRatingList: UserRatingList = UserRatingList.makeInstanceFromSnapshot(userOfRatingSnapshot.data());


            const indexToRemove: number = userRatingList.getUserRatingsList().findIndex(userRatingTemp => userRatingTemp.getUserThatIsRating() === raterOfDeletionFromContext);
            if (indexToRemove === -1)
                throw new ValidationException("Έχει ήδη διαγραφεί")

            userRatingList.getUserRatingsList().splice(indexToRemove, 1);


            await this.userPrivacyFirebaseDao.rateUserTransaction(transaction, userRatingList);

            return null;
        });

    }
}
