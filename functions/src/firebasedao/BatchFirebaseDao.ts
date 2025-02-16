/* eslint-disable */


import * as admin from "firebase-admin";
import {WriteResult} from "firebase-admin/lib/firestore";
import {Batch} from "../models/Batch";
import {logger} from "firebase-functions";

export class BatchFirebaseDao {

    public getAllBatchesFromEverySportPromise(sports:string[]):Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>>[]{

        const promises:Promise<FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>>[] = [];
        sports.forEach((sport:string) => {

            promises.push(admin.firestore().collection('matchesBatch').doc(sport)
                .collection("matchesBatchById")
                .where("empty", "==", false)
                .get());

        })

        return promises;
    }

    public saveBatchesPromise(batchesToSave: Batch[]): Promise<FirebaseFirestore.WriteResult>[] {
        const promises: Promise<WriteResult> [] = [];

        batchesToSave.forEach((batch: Batch) => {
            logger.info("batch",batch);
            const promise: Promise<WriteResult> = admin.firestore().collection('matchesBatch').doc(batch.getSport())
                .collection("matchesBatchById").doc(batch.getId()).set(batch.toObject());

            promises.push(promise);
        });

        logger.info("promises",promises);
        return promises;
    }
}
