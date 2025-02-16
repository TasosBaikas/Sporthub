/* eslint-disable */

import {TerrainAddressFirebaseDao} from "../firebasedao/TerrainAddressFirebaseDao";
import {TerrainAddress} from "../models/TerrainAddress";

export class TerrainAddressRepository {

    private terrainAddressFirebaseDao:TerrainAddressFirebaseDao;

    constructor() {
        this.terrainAddressFirebaseDao = new TerrainAddressFirebaseDao();
    }

    public updateTerrainAddress(terrainAddressMap:Map<string,TerrainAddress[]>, yourId:string):Promise<FirebaseFirestore.WriteResult>{
        return this.terrainAddressFirebaseDao.saveTerrainAddress(terrainAddressMap,yourId);
    }

    public deleteAllTerrainsForThatUser(userId: string): Promise<FirebaseFirestore.WriteResult> {

        return this.terrainAddressFirebaseDao.deleteAllTerrainsForThatUser(userId);
    }

}
