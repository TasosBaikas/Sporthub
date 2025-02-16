/* eslint-disable */

import {MatchFirebaseDao} from "../firebasedao/MatchFirebaseDao";
import {Match} from "../models/Match";
import {DocumentSnapshot} from "firebase-admin/lib/firestore";
import * as admin from "firebase-admin";
import {ValidationException} from "../exceptions/ValidationException";
import {UserShortForm} from "../models/user/UserShortForm";
import {ChatMessage} from "../models/chat/ChatMessage";
import {Chat} from "../models/chat/Chat";
import {ChatFirebaseDao} from "../firebasedao/ChatFirebaseDao";
import {v4 as uuidv4} from "uuid";
import {MatchesBatchFirebaseDao} from "../firebasedao/MatchesBatchFirebaseDao";
import {SportConstants} from "../models/constants/SportConstants";
import {ChatMessageBatch} from "../models/chat/ChatMessageBatch";
import {ChatMessageFirebaseDao} from "../firebasedao/ChatMessageFirebaseDao";
import {UserChatAction} from "../models/userprivacy/UserChatAction";
import {UserPrivacyFirebaseDao} from "../firebasedao/UserPrivacyFirebaseDao";
import {UserSecurityFirebaseDao} from "../firebasedao/UserSecurityFirebaseDao";
import {UserMatchAdminLimit} from "../models/user/usersecurity/UserMatchAdminLimit";

export class MatchRepository {

    private matchFirebaseDao:MatchFirebaseDao;
    private matchesBatchFirebaseDao:MatchesBatchFirebaseDao;
    private chatMessageFirebaseDao:ChatMessageFirebaseDao;

    private chatFirebaseDao:ChatFirebaseDao;
    private userPrivacyFirebaseDao:UserPrivacyFirebaseDao;
    private userSecurityFirebaseDao:UserSecurityFirebaseDao;


    constructor() {
        this.matchFirebaseDao = new MatchFirebaseDao();
        this.matchesBatchFirebaseDao = new MatchesBatchFirebaseDao();
        this.chatMessageFirebaseDao = new ChatMessageFirebaseDao();

        this.chatFirebaseDao = new ChatFirebaseDao();
        this.userPrivacyFirebaseDao = new UserPrivacyFirebaseDao();
        this.userSecurityFirebaseDao = new UserSecurityFirebaseDao();

    }


    public async getMatchByIdPromise(matchId:string,sport:string): Promise<Match> {

        const matchSnapshot:DocumentSnapshot = await this.matchFirebaseDao.getMatchById(matchId,sport)

        return Match.makeInstanceFromSnapshot(matchSnapshot.data());
    }

    public async getMatchesByAdminIdAllSportsPromise(adminId:string): Promise<Match[]> {

        const snapshotsPromises:Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>>[] = [];
        for (const sport of SportConstants.SPORTSLIST.values()) {
            snapshotsPromises.push(this.matchFirebaseDao.getMatchesByAdminIdPromise(adminId,sport));
        }

        const querySnapshots: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>[] = await Promise.all(snapshotsPromises);

        const matches: Match[] = [];
        querySnapshots.forEach((querySnapshot: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>) => {

            querySnapshot.docs.forEach((snapshot: FirebaseFirestore.QueryDocumentSnapshot<FirebaseFirestore.DocumentData>) => {
                // Correctly calling data() method to get document data
                matches.push(Match.makeInstanceFromSnapshot(snapshot.data()));
            });

        });

        return matches;
    }


    public async saveMatch(match: Match,yourId:string): Promise<void> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            match.setCreatedAtUTC(Date.now());

            const matchSnapshot:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction,match.getId(),match.getSport());
            if (matchSnapshot !== undefined && matchSnapshot.exists)
                throw new ValidationException('match already exists');

            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction,match.getId());
            if (chatSnapshot !== undefined && chatSnapshot.exists)
                throw new ValidationException('chat already exists');

            const userActivitySnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getUserActivityToAChatTransaction(transaction, yourId);

            const userMatchLimitSnapshot:DocumentSnapshot = await this.userSecurityFirebaseDao.getUserMatchAdminLimitTransaction(transaction, yourId);


            if (!match.isAdmin(yourId))
                throw new ValidationException('You must be admin to create a match.');

            if (!match.isMember(yourId))
                throw new ValidationException('You must be a member.');

            if (match.getUsersInChat().length !== 1)
                throw new ValidationException('You must be the only 1 in the match.');


            if (match.getMatchDateInUTC() < Date.now())
                throw new ValidationException('Η ώρα που επιλέξατε είναι πριν το "τώρα"');

            const after16Days:number = Date.now() + 16 * 24 * 60 * 60 * 1000;
            if (match.getMatchDateInUTC()  > after16Days)
                throw new ValidationException('Η ώρα που επιλέξατε είναι πάνω απο 16 ημέρες');


            let userMatchAdminLimit:UserMatchAdminLimit;
            if (userMatchLimitSnapshot === undefined || !userMatchLimitSnapshot.exists){

                userMatchAdminLimit = UserMatchAdminLimit.makeInstanceFromValues(yourId, []);
            }else
                userMatchAdminLimit = UserMatchAdminLimit.makeInstanceFromSnapshot(userMatchLimitSnapshot.data());


            const matchesMatchDateTime:number[] = userMatchAdminLimit.getMatchesMatchDateTime();
            const matchesMatchDateTimeThatAreValid: number[] = matchesMatchDateTime
                .filter((matchDateTimeInMilliUTC:number) => matchDateTimeInMilliUTC > Date.now());


            matchesMatchDateTimeThatAreValid.push(match.getMatchDateInUTC());

            if (matchesMatchDateTimeThatAreValid.length > UserMatchAdminLimit.MAX_MATCHES)
                throw new ValidationException('Έχετε δημιουργήσει πολλές ομάδες.');

            userMatchAdminLimit.setMatchesMatchDateTime(matchesMatchDateTimeThatAreValid);

            const adminId:string = match.getAdmin().getUserId();

            const chatMembers:string[] = [];
            chatMembers.push(adminId);


            const admin:UserShortForm = match.getAdmin();

            //now we make the first chatmessage
            const chatMessageBatchId: string = uuidv4();

            const firstChatMessage: ChatMessage = ChatMessage.chatFirstMessageInstance(chatMessageBatchId, match.getId(), admin);

            const chatMessageBatch: ChatMessageBatch = ChatMessageBatch.makeChatMessageBatch(chatMessageBatchId, firstChatMessage.getChatId(),
                [firstChatMessage], [], [firstChatMessage.getUserShortForm().getUserId()], Date.now());



            const chat:Chat = Chat.makeChatMatchConversation(match.getId(), match.getSport(),
                adminId,match.getMatchDetailsFromAdmin(),
                match.getMatchDateInUTC(),match.getMatchDuration(),match.getHasTerrainType(),match.getTerrainAddress(),firstChatMessage,null,[],chatMembers);



            let userChatAction:UserChatAction;
            if (userActivitySnapshot == null || !userActivitySnapshot.exists){
                userChatAction = UserChatAction.makeInstance(yourId, []);
            }else{
                userChatAction = UserChatAction.makeInstanceFromSnapshot(userActivitySnapshot.data());
            }

            if (!userChatAction.getChatsIds().includes(chat.getId())){

                userChatAction.getChatsIds().push(chat.getId());

                this.userPrivacyFirebaseDao.updateUserActivityToAChat(transaction, userChatAction);
            }

            this.chatMessageFirebaseDao.saveChatMessageBatchTransactional(transaction, chatMessageBatch);

            this.chatFirebaseDao.saveChatTransaction(transaction,chat);

            this.matchFirebaseDao.saveMatchTransaction(transaction,match);
            this.matchesBatchFirebaseDao.saveMatchInTemporalHolderTransaction(transaction,match);

            this.userSecurityFirebaseDao.saveUserMatchAdminLimitTransaction(transaction, userMatchAdminLimit);

        });

    }

    async updateMatchIfAdminChangeSomeValues(updatedMatch: Match, yourId:string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const matchDocSnap:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction, updatedMatch.getId(), updatedMatch.getSport());
            await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,updatedMatch.getId(), updatedMatch.getSport());
            if (matchDocSnap == null || !matchDocSnap.exists)
                throw new ValidationException("Μάλλον η ομάδα διαγράφηκε");

            const matchFromFirebase:Match = Match.makeInstanceFromSnapshot(matchDocSnap.data());

            if (!matchFromFirebase.getUsersInChat().includes(yourId))
                throw new ValidationException("Δεν είστε στην ομάδα");

            if (!matchFromFirebase.isAdmin(yourId))
                throw new ValidationException("Δεν είστε διαχειριστής");


            matchFromFirebase.setAdmin(updatedMatch.getAdmin());

            matchFromFirebase.setLatitude(updatedMatch.getLatitude());
            matchFromFirebase.setLongitude(updatedMatch.getLongitude());

            matchFromFirebase.setMatchDuration(updatedMatch.getMatchDuration());

            matchFromFirebase.setMatchDetailsFromAdmin(updatedMatch.getMatchDetailsFromAdmin());

            matchFromFirebase.setLevels(updatedMatch.getLevels());

            matchFromFirebase.setHasTerrainType(updatedMatch.getHasTerrainType());
            matchFromFirebase.setTerrainAddress(updatedMatch.getTerrainAddress());


            this.matchFirebaseDao.updateMatchTransaction(transaction, matchFromFirebase);
            this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction, matchFromFirebase);
            return null;
        });

    }


    async userOnlyJoinMatch(requesterId:string,matchId:string,sport:string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const matchDocSnap:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction, matchId, sport);
            await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,matchId,sport);
            if (matchDocSnap == null || !matchDocSnap.exists)
                throw new ValidationException("Μάλλον η ομάδα διαγράφηκε");

            const matchFromFirebase:Match = Match.makeInstanceFromSnapshot(matchDocSnap.data());

            if (matchFromFirebase.getUsersInChat().includes(requesterId))
                throw new ValidationException("Είστε ήδη στην ομάδα!");

            if (matchFromFirebase.getMatchDateInUTC() < Date.now())
                throw new ValidationException("Ο αγώνας έχει λήξει");


            if (matchFromFirebase.isUserRequestedToJoin(requesterId))
                throw new ValidationException("Έχετε κάνει ήδη αίτηση!");


            matchFromFirebase.getUserRequestsToJoinMatch().push(requesterId);

            this.matchFirebaseDao.updateMatchTransaction(transaction, matchFromFirebase);
            this.matchesBatchFirebaseDao.saveMatchInTemporalHolderTransaction(transaction,matchFromFirebase);
        });
    }

    async userJoinOrCancelRequestForMatch(requesterId: string, matchId: string, sport: string): Promise<Match> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const matchDocSnap:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction, matchId, sport);
            let matchFromTemporal:any = await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction, matchId, sport);
            if (matchDocSnap == null || !matchDocSnap.exists)
                throw new ValidationException("Μάλλον η ομάδα διαγράφηκε");

            const matchFromFirebase:Match = Match.makeInstanceFromSnapshot(matchDocSnap.data());

            if (matchFromFirebase.isMember(requesterId))
                throw new ValidationException("Είστε ήδη στην ομάδα");

            if (matchFromFirebase.getMatchDateInUTC() < Date.now())
                throw new ValidationException("Ο αγώνας έχει λήξει");


            if (!matchFromFirebase.isUserRequestedToJoin(requesterId)) {
                matchFromFirebase.getUserRequestsToJoinMatch().push(requesterId);
            } else {
                const indexToRemove: number = matchFromFirebase.getUserRequestsToJoinMatch().indexOf(requesterId);

                if (indexToRemove !== -1) {
                    matchFromFirebase.getUserRequestsToJoinMatch().splice(indexToRemove, 1);
                }
            }

            matchFromTemporal = Match.makeCopy(matchFromFirebase);

            console.log("userJoinOrCancelRequestForMatch before updateMatchTransaction", matchFromFirebase);
            this.matchFirebaseDao.updateMatchTransaction(transaction, matchFromFirebase);

            console.log("userJoinOrCancelRequestForMatch before updateMatchInTemporalHolderTransaction", matchFromTemporal);
            this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction, matchFromTemporal);

            return matchFromFirebase;
        });
    }

    async adminAcceptsUser(userToAccept: string, adminId:string, matchId: string, sport: string): Promise<Match> {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const matchSnapshot:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction,matchId,sport);
            let matchFromTemporal:any = await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,matchId,sport);
            const chatSnapshot:DocumentSnapshot = await this.chatFirebaseDao.getChatByIdTransaction(transaction,matchId);

            const userActivitySnapshot:DocumentSnapshot = await this.userPrivacyFirebaseDao.getUserActivityToAChatTransaction(transaction, userToAccept);

            if (matchSnapshot == null || !matchSnapshot.exists)
                throw new ValidationException("Μάλλον η ομάδα διαγράφηκε");

            const matchFromFirebase:Match = Match.makeInstanceFromSnapshot(matchSnapshot.data());

            if (!matchFromFirebase.isAdmin(adminId))
                throw new ValidationException("Δεν είστε ο admin!");

            if (chatSnapshot == null || !chatSnapshot.exists)
                throw new ValidationException("Bug: Λείπει το chat σε αυτην την ομάδα");


            if (matchFromFirebase.getUsersInChat().includes(userToAccept))
                throw new ValidationException("Ο χρήστης είναι ήδη στην ομάδα");


            if (!matchFromFirebase.isUserRequestedToJoin(userToAccept))
                throw new ValidationException("Ο χρήστης ακύρωσε την αίτηση του");


            const indexToRemoveFirst: number = matchFromFirebase.getUserRequestsToJoinMatch().indexOf(userToAccept);
            if (indexToRemoveFirst !== -1) {
                matchFromFirebase.getUserRequestsToJoinMatch().splice(indexToRemoveFirst, 1);
            }

            const indexToRemoveSecond = matchFromFirebase.getAdminIgnoredRequesters().indexOf(userToAccept);
            if (indexToRemoveSecond !== -1) {
                matchFromFirebase.getAdminIgnoredRequesters().splice(indexToRemoveSecond, 1);
            }

            matchFromFirebase.getUsersInChat().push(userToAccept);


            const chatFromFirebase:Chat = Chat.makeInstanceFromSnapshot(chatSnapshot.data());

            if (!chatFromFirebase.getMembersIds().includes(userToAccept))
                chatFromFirebase.getMembersIds().push(userToAccept);



            let userChatAction:UserChatAction;
            if (userActivitySnapshot == null || !userActivitySnapshot.exists){
                userChatAction = UserChatAction.makeInstance(userToAccept, []);
            }else{
                userChatAction = UserChatAction.makeInstanceFromSnapshot(userActivitySnapshot.data());
            }


            if (!userChatAction.getChatsIds().includes(chatFromFirebase.getId())){

                userChatAction.getChatsIds().push(chatFromFirebase.getId());

                this.userPrivacyFirebaseDao.updateUserActivityToAChat(transaction, userChatAction);
            }

            matchFromTemporal = Match.makeCopy(matchFromFirebase);

            console.log("userJoinOrCancelRequestForMatch before updateMatchTransaction", matchFromFirebase);
            this.matchFirebaseDao.updateMatchTransaction(transaction, matchFromFirebase);

            console.log("userJoinOrCancelRequestForMatch before updateMatchInTemporalHolderTransaction", matchFromTemporal);
            this.matchesBatchFirebaseDao.updateMatchInTemporalHolderTransaction(transaction,matchFromFirebase);
            this.chatFirebaseDao.updateChatTransactional(transaction,chatFromFirebase);

            return matchFromFirebase;
        });
    }

    async ignoreRequesterIfAdmin(userToIgnore: string, adminId: string, matchId: string, sport: string) {

        return admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

            const matchDocSnap:DocumentSnapshot = await this.matchFirebaseDao.getMatchByIdTransaction(transaction, matchId, sport);
            await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolderTransaction(transaction,matchId,sport);
            if (matchDocSnap == null || !matchDocSnap.exists)
                throw new ValidationException("Μάλλον η ομάδα διαγράφηκε");

            const matchFromFirebase:Match = Match.makeInstanceFromSnapshot(matchDocSnap.data());

            if (matchFromFirebase.getUsersInChat().includes(userToIgnore))
                throw new ValidationException("Ο χρήστης είναι ήδη στην ομάδα");

            if (!matchFromFirebase.isAdmin(adminId))
                throw new ValidationException("Δεν είστε διαχειριστής");

            if (matchFromFirebase.getAdminIgnoredRequesters().includes(userToIgnore)) {

                const indexToRemove = matchFromFirebase.getAdminIgnoredRequesters().indexOf(userToIgnore);
                if (indexToRemove !== -1) {
                    matchFromFirebase.getAdminIgnoredRequesters().splice(indexToRemove, 1);
                }
            } else {
                matchFromFirebase.getAdminIgnoredRequesters().push(userToIgnore);
            }


            this.matchFirebaseDao.updateMatchTransaction(transaction, matchFromFirebase);
            this.matchesBatchFirebaseDao.saveMatchInTemporalHolderTransaction(transaction, matchFromFirebase);

            return matchFromFirebase;
        });

    }
}
