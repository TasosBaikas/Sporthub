/* eslint-disable */



import {ValidationException} from "../../../exceptions/ValidationException";

const functions = require("firebase-functions");
import {TerrainAddressRepository} from "../../../repository/TerrainAddressRepository";
import {TerrainAddress} from "../../../models/TerrainAddress";
import {ElementNotFoundException} from "../../../exceptions/ElementNotFoundException";


const terrainAddressRepository:TerrainAddressRepository = new TerrainAddressRepository();


export const terrainRepositoryUpdateTerrain = functions.region('europe-west1').https.onCall(async (data: any, context: any) => {


    try {

        if (!context.auth)
            throw new ValidationException('You must be authenticated to call this function.');


        let parsedSnapshot;
        try{
            parsedSnapshot = JSON.parse(data);
        }catch (Error){
            parsedSnapshot = data;
        }


        const yourId:string = parsedSnapshot.yourId;
        if (yourId !== context.auth.uid)
            throw new ValidationException('You must be authenticated to call this function.');

        let parsedTerrainSnapshot;
        try{
            parsedTerrainSnapshot = JSON.parse(parsedSnapshot.terrainAddressesMap);
        }catch (Error){
            parsedTerrainSnapshot = parsedSnapshot.terrainAddressesMap;
        }




        const map: Map<string, TerrainAddress[]> = new Map();
        for (const key of Object.keys(parsedTerrainSnapshot)) {

            if (parsedTerrainSnapshot.hasOwnProperty(key)) {
                const value = parsedTerrainSnapshot[key];

                // Ensure that the value is an array

                const terrainAddressList:TerrainAddress[] = [];
                for (const element of value) {
                    terrainAddressList.push(TerrainAddress.makeInstanceFromSnapshot(element));
                }
                map.set(key, terrainAddressList);

            }
        }

        await terrainAddressRepository.updateTerrainAddress(map,yourId);

        return true;

    } catch (error) {

        if (error instanceof ValidationException)
            throw new functions.https.HttpsError('invalid-argument', error.message);

        if (error instanceof ElementNotFoundException)
            throw new functions.https.HttpsError('not-found', "Error fetching data");


        throw new functions.https.HttpsError('internal', 'An internal error occurred.');
    }
});

