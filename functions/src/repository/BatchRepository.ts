/* eslint-disable */


import {Batch} from "../models/Batch";
import {BatchFirebaseDao} from "../firebasedao/BatchFirebaseDao";
import {WriteResult} from "firebase-admin/lib/firestore";

export class BatchRepository {

    private batchFirebaseDao:BatchFirebaseDao;


    constructor() {
        this.batchFirebaseDao = new BatchFirebaseDao();
    }

    public async getBatchesPromise(sports:string[]): Promise<Batch[]> {

        const snapshotOfAllBatches: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData>[] = await Promise.all(this.batchFirebaseDao.getAllBatchesFromEverySportPromise(sports));

        const allBatches:Batch[] = [];
        // Then, when retrieving data:
        snapshotOfAllBatches.forEach(snapshot => {

            snapshot.docs.forEach(doc => {
                const data = doc.data();
                const batchInstance:Batch = Batch.makeInstanceFromSnapshot(data);

                allBatches.push(batchInstance);
            });

        });

        return allBatches;
    }

    public saveBatchesPromise(batchesToSave: Batch[]): Promise<WriteResult> [] {

        return this.batchFirebaseDao.saveBatchesPromise(batchesToSave);

    }

}
