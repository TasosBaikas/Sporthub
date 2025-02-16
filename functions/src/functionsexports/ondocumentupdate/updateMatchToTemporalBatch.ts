/* eslint-disable */


import {Match} from "../../models/Match";
import {MatchBatchUpdateTableRepository} from "../../repository/MatchBatchUpdateTableRepository";

const functions = require("firebase-functions");

export const updateMatchToTemporalBatch = functions.region('europe-west1').firestore
  .document('matchesBatch/{sport}/temporalMatchHolder/{matchById}')
  .onWrite(async (change:any, context: any) => {

    if (change.after.data() === null || change.after.data() === undefined)
      return;

    const matchAfter:Match = Match.makeInstanceFromSnapshot(change.after.data());

    const matchUpdateTable:MatchBatchUpdateTableRepository = new MatchBatchUpdateTableRepository();

    await matchUpdateTable.updateToTrueBatchUpdateTable(matchAfter.getSport());

    return null;
});



