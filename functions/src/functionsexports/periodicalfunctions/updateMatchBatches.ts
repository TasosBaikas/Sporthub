/* eslint-disable */

import {MatchBatchUpdateTableRepository} from "../../repository/MatchBatchUpdateTableRepository";

const functions = require("firebase-functions");

import { logger } from "firebase-functions";
import {MatchesBatchRepository} from "../../repository/MatchesBatchRepository";


const matchBatchUpdateTableRepository:MatchBatchUpdateTableRepository = new MatchBatchUpdateTableRepository();
const matchesBatchRepository:MatchesBatchRepository = new MatchesBatchRepository();


export const updateMatchBatches = functions.region('europe-west1').pubsub.schedule('* * * * *')
    .timeZone('UTC')
    .onRun(async (context: any) => {//must have more time of execution


    // Get a document from Firestore
    const updateTable:Map<string, boolean> | undefined = await matchBatchUpdateTableRepository.updateTableReturnPreviousValueTransaction();
    logger.info("updateTable",updateTable);
    if (updateTable === undefined){
        return null;
    }

    const promises:Promise<any>[] = [];
    for (const [sport, hasUpdates] of updateTable.entries()) {
        if (!hasUpdates)
            continue;

        promises.push(matchesBatchRepository.updateBatchesFromTemporal(sport));
    }

    if (promises.length == 0)
        return null;

    await Promise.allSettled(promises);

    return null;

});
