/* eslint-disable */

import * as admin from "firebase-admin";
import {Match} from "../models/Match";

export class MatchFirebaseDao {

    public async getBatchesThatAreNotEmpty(sport:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {

        return admin.firestore().collection('matchesBatch').doc(sport)
            .collection("matchesBatchById")
            .where("empty", "==", false)
            .get()

    }

    public getMatchByIdTransaction(transaction: FirebaseFirestore.Transaction, matchId:string,sport:string) {

        const matchDoc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("matches").doc(sport)
            .collection("matchesById").doc(matchId);

        return transaction.get(matchDoc);
    }

    public getMatchById(matchId: string, sport:string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {

        const matchDoc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("matches").doc(sport)
            .collection("matchesById").doc(matchId);

        return matchDoc.get();
    }

    public updateMatchTransaction(transaction: FirebaseFirestore.Transaction, match: Match): FirebaseFirestore.Transaction {
        match.setInTemporalMatch(false);

        const matchDoc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("matches").doc(match.getSport())
            .collection("matchesById").doc(match.getId());

        return transaction.set(matchDoc,match.toObject());
    }

    deleteMatchTransaction(transaction: FirebaseFirestore.Transaction, matchId: string, sport: string) {
        const matchDoc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("matches").doc(sport)
            .collection("matchesById").doc(matchId);

        return transaction.delete(matchDoc);
    }


    saveMatchTransaction(transaction: FirebaseFirestore.Transaction, match: Match): FirebaseFirestore.Transaction {
        match.setInTemporalMatch(false);

        const matchDoc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("matches").doc(match.getSport())
            .collection("matchesById").doc(match.getId());

        return transaction.set(matchDoc,match.toObject());

    }

    async getMatchesByAdminIdPromise(adminId: string, sport:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {

        const matchCol: FirebaseFirestore.CollectionReference<FirebaseFirestore.DocumentData> = admin.firestore().collection("matches").doc(sport)
            .collection("matchesById");

        return matchCol
            .where('admin.userId', '==', adminId)
            .get();
    }

}
