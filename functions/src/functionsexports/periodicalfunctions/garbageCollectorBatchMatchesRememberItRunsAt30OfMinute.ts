/* eslint-disable */

import {SportConstants} from "../../models/constants/SportConstants";

const functions = require("firebase-functions");
import {WriteResult } from "firebase-admin/firestore";
import { Batch } from "../../models/Batch";
import {BatchRepository} from "../../repository/BatchRepository";



export const garbageCollectorBatchMatchesRememberItRunsAt30OfMinute = functions.region('europe-west1').pubsub.schedule('30 2 * * *')
    .timeZone('UTC')
    .onRun(async (context: any) => {


    const sportsList: string[] = SportConstants.SPORTSLIST;


    try{

        await delay(10000);

        const batchRepository:BatchRepository = new BatchRepository();


        const allBatches:Batch[] = await batchRepository.getBatchesPromise(sportsList);
        // Then, when retrieving data:
        allBatches.forEach(batch => {

            batch.removeExpiredMatchesFromBatch();
        });

        const promisesToSaveBatches:Promise<WriteResult>[] = batchRepository.saveBatchesPromise(allBatches);

        await Promise.allSettled(promisesToSaveBatches).then((results) => {
            results.forEach((result, index) => {
                if(result.status === 'fulfilled') {
                    console.log(`Promise ${index} fulfilled with value: ${result.value}`);
                } else {
                    console.error(`Promise ${index} rejected with reason: ${result.reason}`);
                }
            });
        });

        return null;
    } catch (error) {
        console.error('Error fetching or processing data:', error);
        return null;
    };

});

function delay(ms:number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}


