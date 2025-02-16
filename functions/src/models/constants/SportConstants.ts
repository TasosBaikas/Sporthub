/* eslint-disable */


export class SportConstants {

    public static readonly TENNIS: string = "tennis";
    public static readonly VOLLEYBALL:string = "volleyball";
    public static readonly PINGPONG:string = "pingPong";
    public static readonly FOOTBALL:string = "football";
    public static readonly BASKETBALL:string = "basketball";
    public static readonly BEACH_VOLLEY:string = "beach_volley";
    public static readonly PADEL:string = "padel";
    public static readonly CYCLING:string = "cycling";

    public static SPORTSLIST: string[];

    static {

        SportConstants.SPORTSLIST = [];
        SportConstants.SPORTSLIST.push(SportConstants.TENNIS);
        SportConstants.SPORTSLIST.push(SportConstants.VOLLEYBALL);
        SportConstants.SPORTSLIST.push(SportConstants.PINGPONG);
        SportConstants.SPORTSLIST.push(SportConstants.FOOTBALL);
        SportConstants.SPORTSLIST.push(SportConstants.BASKETBALL);
        SportConstants.SPORTSLIST.push(SportConstants.BEACH_VOLLEY);
        SportConstants.SPORTSLIST.push(SportConstants.PADEL);
        SportConstants.SPORTSLIST.push(SportConstants.CYCLING);

    }

}

