/* eslint-disable */


import {TerrainAddress} from "../models/TerrainAddress";
import * as admin from "firebase-admin";

export class TerrainAddressFirebaseDao {


    public saveTerrainAddress(terrainAddressMap: Map<string, TerrainAddress[]>, yourId: string): Promise<FirebaseFirestore.WriteResult> {

        const doc = admin.firestore().collection("userSavedTerrainAddresses").doc(yourId);

        const record: Record<string, {}> = {};
        terrainAddressMap.forEach((value, key) => {

            const toPlain: {}[] = [];
            value.forEach((terrain: TerrainAddress) => {
                toPlain.push(terrain.toObject());
            });


            record[key] = toPlain;
        });

        return doc.set(record);
    }

    public deleteAllTerrainsForThatUser(userId: string): Promise<FirebaseFirestore.WriteResult> {

        const doc = admin.firestore().collection("userSavedTerrainAddresses").doc(userId);

        return doc.delete();
    }

}
