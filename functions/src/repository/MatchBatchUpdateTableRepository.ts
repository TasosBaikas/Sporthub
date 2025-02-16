/* eslint-disable */


import * as admin from "firebase-admin";
import {DocumentSnapshot} from "firebase-admin/lib/firestore";

export class MatchBatchUpdateTableRepository {

    public async updateToTrueBatchUpdateTable(sport:string): Promise<FirebaseFirestore.WriteResult> {

        const docRef = admin.firestore().collection('matchBatchUpdateTable').doc('updateTable');

        // Prepare the update object
        const updateObject:Record<string, boolean> = {};
        updateObject[sport] = true;

        // Perform the update
        return docRef.update(updateObject);
    }

    public async updateTableReturnPreviousValueTransaction(): Promise<Map<string, boolean> | undefined> {
        try {
            // Run a transaction
            const result: undefined | FirebaseFirestore.DocumentData =  await admin.firestore()
                .runTransaction(async (transaction) => {

                    const docRef = admin.firestore().collection('matchBatchUpdateTable').doc('updateTable');
                    const doc:DocumentSnapshot = await transaction.get(docRef);

                    if (!doc.exists) {
                        return undefined;
                    }

                    // Retrieve and modify the document data
                    const data: FirebaseFirestore.DocumentData | undefined = doc.data();
                    if (data === undefined){
                        return undefined;
                    }
                    const dataMap: Map<string, boolean> = new Map(Object.entries(data) as [string, boolean][]);

                    dataMap.forEach((value, key) => {
                        dataMap.set(key, false);
                    });

                    // Convert Map back to an object since Firestore doesn't support Map
                    const dataObject = Object.fromEntries(dataMap);
                    transaction.set(docRef, dataObject); // Or use transaction.update(docRef, dataObject) if you are updating an existing doc

                    return data; // Return the original data
            });

            if (result === undefined)
                return undefined;

            return new Map(Object.entries(result) as [string, boolean][]);
        } catch (e) {
            console.log('Transaction failure:', e);
            return undefined; // You may want to handle the error differently
        }
    }

}
