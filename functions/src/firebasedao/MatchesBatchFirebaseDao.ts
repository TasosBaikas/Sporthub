/* eslint-disable */

import * as admin from "firebase-admin";
import {Batch} from "../models/Batch";
import {Match} from "../models/Match";
import {firestore} from "firebase-admin";
import Transaction = firestore.Transaction;

export class MatchesBatchFirebaseDao {

    public saveBatch(batch:Batch): Promise<FirebaseFirestore.WriteResult> {

        return admin.firestore().collection('matchesBatch').doc(batch.getSport())
            .collection("matchesBatchById").doc(batch.getId())
            .set(batch.toObject())
    }

    public getBatchesThatAreNotEmpty(sport:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {

        return admin.firestore().collection('matchesBatch').doc(sport)
            .collection("matchesBatchById")
            .where("empty", "==", false)
            .get()

    }

    public getMatchFromTemporalMatchHolder(matchId:string,sport:string): Promise<FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData>> {

        return admin.firestore().collection('matchesBatch').doc(sport)
            .collection("temporalMatchHolder")
            .doc(matchId)
            .get()

    }

    async getMatchFromTemporalMatchHolderTransaction(transaction: FirebaseFirestore.Transaction, matchId: string, sport: string) {
        const doc: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> =  admin.firestore().collection('matchesBatch').doc(sport)
            .collection("temporalMatchHolder")
            .doc(matchId);

        return transaction.get(doc);
    }

    public getAllMatchesFromTemporalMatchHolderTransactional(transaction:Transaction, sport:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
        const col =  admin.firestore().collection('matchesBatch').doc(sport)
            .collection("temporalMatchHolder");

        return transaction.get(col);
    }


    public getAllMatchesFromTemporalMatchHolder(sport:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>> {
        return admin.firestore().collection('matchesBatch').doc(sport)
            .collection("temporalMatchHolder")
            .get();
    }

    public getMatchesBatchThatAreNotFull(sport:string): Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>>{

        return admin.firestore().collection('matchesBatch').doc(sport)
            .collection("matchesBatchById")
            .where("countMatches" , "<=" , Batch.MAX_SIZE_BATCH)
            .get();

    }

    public getFromTemporalMatches(sport: string) {
        return admin.firestore().collection('matchesBatch').doc(sport)
            .collection("temporalMatchHolder")
            .get();
    }

    public saveMatchInTemporalHolderTransaction(transaction: FirebaseFirestore.Transaction, match: Match): FirebaseFirestore.Transaction {
        match.setInTemporalMatch(true);

        const docRef: FirebaseFirestore.DocumentReference<FirebaseFirestore.DocumentData> =  admin.firestore().collection('matchesBatch').doc(match.getSport())
            .collection("temporalMatchHolder").doc(match.getId());


        return transaction.set(docRef,match.toObject());
    }

    public updateMatchInTemporalHolderTransaction(transaction: FirebaseFirestore.Transaction, match: Match): FirebaseFirestore.Transaction {
        match.setInTemporalMatch(true);

        return this.saveMatchInTemporalHolderTransaction(transaction,match);
    }


    getMatchBatchThatIsNotFullTransaction(transaction: FirebaseFirestore.Transaction, sport: string) {
        const col =  admin.firestore().collection('matchesBatch').doc(sport)
            .collection("matchesBatchById")
            .where("countMatches" , "<=" , Batch.MAX_SIZE_BATCH)
            .orderBy("countMatches")
            .limit(1);

        return transaction.get(col);
    }

    saveBatchTransactional(transaction: FirebaseFirestore.Transaction, batch: Batch): Transaction {
        const col =  admin.firestore().collection('matchesBatch').doc(batch.getSport())
            .collection("matchesBatchById").doc(batch.getId());

        return transaction.set(col, batch.toObject());
    }

    deleteMatchesFromTemporal(transaction: FirebaseFirestore.Transaction, matchToSave: Match) {
        const doc = admin.firestore().collection("matchesBatch").doc(matchToSave.getSport())
            .collection("temporalMatchHolder").doc(matchToSave.getId());

        transaction.delete(doc);
    }
}
