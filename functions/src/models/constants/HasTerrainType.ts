/* eslint-disable */


export class HasTerrainTypes {
    public static readonly I_HAVE_CERTAIN_TERRAIN: string = "iHaveCertainTerrain";
    public static readonly I_HAVE_NOT_CERTAIN_TERRAIN: string = "iHaveNotCertainTerrain";
    public static readonly I_DONT_HAVE_TERRAIN: string = "iDontHaveTerrain";
    public static TERRAIN_OPTIONS_ENGLISH_LIST: string[];

    static {

        HasTerrainTypes.TERRAIN_OPTIONS_ENGLISH_LIST = [];
        HasTerrainTypes.TERRAIN_OPTIONS_ENGLISH_LIST.push(HasTerrainTypes.I_HAVE_CERTAIN_TERRAIN);
        HasTerrainTypes.TERRAIN_OPTIONS_ENGLISH_LIST.push(HasTerrainTypes.I_HAVE_NOT_CERTAIN_TERRAIN);
        HasTerrainTypes.TERRAIN_OPTIONS_ENGLISH_LIST.push(HasTerrainTypes.I_DONT_HAVE_TERRAIN);

    }

}
