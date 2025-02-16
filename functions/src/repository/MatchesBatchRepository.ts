/* eslint-disable */

import {MatchesBatchFirebaseDao} from "../firebasedao/MatchesBatchFirebaseDao";
import {Match} from "../models/Match";
import {Batch} from "../models/Batch";
import * as admin from "firebase-admin";
import {logger} from "firebase-functions";
import {v4 as uuidv4} from "uuid";

export class MatchesBatchRepository {

    private matchesBatchFirebaseDao:MatchesBatchFirebaseDao;

    constructor() {
        this.matchesBatchFirebaseDao = new MatchesBatchFirebaseDao();
    }


    public async saveBatch(batch:Batch): Promise<FirebaseFirestore.WriteResult> {
        return this.matchesBatchFirebaseDao.saveBatch(batch);
    }

    public async getAllMatchesFromTemporalMatchHolder(sport:string): Promise<Match[]> {

        const snapshots: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.matchesBatchFirebaseDao.getAllMatchesFromTemporalMatchHolder(sport);

        const allMatches:Match[] = [];
        snapshots.docs.forEach((snapshot) => {
            const match:Match = Match.makeInstanceFromSnapshot(snapshot.data());

            allMatches.push(match);
        })


        return allMatches;
    }

    public async getMatchesBatchThatAreNotFull(sport:string): Promise<Batch[]> {

        const allBatches:Batch[] = [];

        const snapshots: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.matchesBatchFirebaseDao.getMatchesBatchThatAreNotFull(sport);
        snapshots.docs.forEach((snapshot) => {

            const batch:Batch = Batch.makeInstanceFromSnapshot(snapshot.data());
            allBatches.push(batch);
        })

        return allBatches;
    }

    public async getMatchesBatchThatAreNotFullAllSports(sports:string[]): Promise<Map<string,Batch[]>> {

        const allBatches:Map<string,Batch[]> = new Map<string, Batch[]>();
        for (const sport of sports) {
            const allMatches:Batch[] = [];



            allBatches.set(sport,allMatches);
        }

        return allBatches;
    }

    public async getMatchFromTemporalMatchHolder(matchId:string,sport:string): Promise<Match | undefined> {

        const snapshot: FirebaseFirestore.DocumentSnapshot<FirebaseFirestore.DocumentData> = await this.matchesBatchFirebaseDao.getMatchFromTemporalMatchHolder(matchId,sport);

        if (snapshot.data() === undefined)
            return undefined;

        return Match.makeInstanceFromSnapshot(snapshot.data());
    }


    public async getAllMatchesFromBatchesThatAreNotEmpty(sport:string): Promise<Match[]> {

        const snapshots: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.matchesBatchFirebaseDao.getBatchesThatAreNotEmpty(sport);

        let allMatches:Match[] = [];
        snapshots.docs.forEach((snapshot) => {
            logger.info("getAllMatchesFromBatchesThatAreNotEmpty");

            const batch:Batch = Batch.makeInstanceFromSnapshot(snapshot.data());
            logger.info("batch",batch);

            allMatches = allMatches.concat(batch.getMatchesList());
        })
        logger.info("allMatches",allMatches);


        return allMatches;
    }

    public async getFromTemporalMatches(sport:string): Promise<Match[]> {

        const snapshots: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.matchesBatchFirebaseDao.getFromTemporalMatches(sport);

        const allMatches:Match[] = [];
        snapshots.docs.forEach((snapshot) => {
            allMatches.push(Match.makeInstanceFromSnapshot(snapshot.data()));
        })
        logger.info("allMatches",allMatches);


        return allMatches;
    }


    async updateBatchesFromTemporal(sport: string) {

        try{
            await admin.firestore().runTransaction(async (transaction: FirebaseFirestore.Transaction) => {

                const matchesSnapshot: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.matchesBatchFirebaseDao.getAllMatchesFromTemporalMatchHolderTransactional(transaction, sport);

                const matchesToSave:Match[] = [];
                matchesSnapshot.docs.forEach((snapshot) => {

                    matchesToSave.push(Match.makeInstanceFromSnapshot(snapshot.data()));
                })

                console.log(1);
                if (matchesToSave.length == 0)
                    return;

                matchesToSave.forEach((match:Match) => match.setInTemporalMatch(false));


                const allBatchesNotFull:Batch[] = [];

                const batchesSnapshot: FirebaseFirestore.QuerySnapshot<FirebaseFirestore.DocumentData> = await this.matchesBatchFirebaseDao.getMatchBatchThatIsNotFullTransaction(transaction, sport);
                batchesSnapshot.docs.forEach((snapshot) => {//the snapshot must have 0 or 1 elements

                    const batch:Batch = Batch.makeInstanceFromSnapshot(snapshot.data());
                    allBatchesNotFull.push(batch);
                })
                console.log(2);


                if (allBatchesNotFull.length === 0){
                    const uuid = uuidv4();

                    const batch:Batch = Batch.makeInstance(uuid,matchesToSave, sport);

                    allBatchesNotFull.push(batch);
                }

                // const theLeftOnes:Match[] = forEachBatchUpdateTheMatches(allBatchesNotFull,matchesToSave);
                // logger.info("theLeftOnes",theLeftOnes);

                allBatchesNotFull[0].addMatchesOrUpdateToBatch(matchesToSave);

                console.log(3);

                this.matchesBatchFirebaseDao.saveBatchTransactional(transaction, allBatchesNotFull[0]);

                console.log(4);

                matchesToSave.forEach((match:Match) => {
                    console.log(5);

                    this.matchesBatchFirebaseDao.deleteMatchesFromTemporal(transaction, match);
                })

                console.log(6);

            });

        } catch (error) {
            console.error("Transaction failed:", error);
            // Check if the error message suggests that an index is needed
            throw error; // or handle it differently
        }



    }
}
