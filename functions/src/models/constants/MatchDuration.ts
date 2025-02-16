/* eslint-disable */


export class MatchDuration {

    public static HOUR_1_GREEK_TEXT:string = "1 Ώρα";
    public static HOUR_1_AND_HALF_GREEK_TEXT:string = "1 Ώρα και 30 λεπτά";
    public static HOURS_2_GREEK_TEXT:string = "2 Ώρες";
    public static HOURS_2_AND_HALF_GREEK_TEXT:string = "2 Ώρες και 30 λεπτά";
    public static HOURS_3_GREEK_TEXT:string = "3 Ώρες";
    public static HOURS_3_AND_MORE_GREEK_TEXT:string = "+3 Ώρες";
    public static HOURS_GREEK_OPTIONS_LIST: Map<string, string> = new Map<string,string>();

    public static ALL_DURATIONS_IN_NUMBERS: number[] = [];

    static {

        MatchDuration.HOURS_GREEK_OPTIONS_LIST.set(MatchDuration.HOUR_1_GREEK_TEXT, "1 Ώρα");
        MatchDuration.HOURS_GREEK_OPTIONS_LIST.set(MatchDuration.HOUR_1_AND_HALF_GREEK_TEXT, "1 Ώρα και 30 λεπτά");
        MatchDuration.HOURS_GREEK_OPTIONS_LIST.set(MatchDuration.HOURS_2_GREEK_TEXT, "2 Ώρες");
        MatchDuration.HOURS_GREEK_OPTIONS_LIST.set(MatchDuration.HOURS_2_AND_HALF_GREEK_TEXT, "2 Ώρες και 30 λεπτά");
        MatchDuration.HOURS_GREEK_OPTIONS_LIST.set(MatchDuration.HOURS_3_GREEK_TEXT, "3 Ώρες");
        MatchDuration.HOURS_GREEK_OPTIONS_LIST.set(MatchDuration.HOURS_3_AND_MORE_GREEK_TEXT, "+3 Ώρες");

        const hour1:number = 60 * 60 * 1000;
        MatchDuration.ALL_DURATIONS_IN_NUMBERS.push(hour1);
        MatchDuration.ALL_DURATIONS_IN_NUMBERS.push(hour1 * 1.5);
        MatchDuration.ALL_DURATIONS_IN_NUMBERS.push(hour1 * 2);
        MatchDuration.ALL_DURATIONS_IN_NUMBERS.push(hour1 * 2.5);
        MatchDuration.ALL_DURATIONS_IN_NUMBERS.push(hour1 * 3);
    }

    public static checkDurationThatIsValidReturnBoolean(duration:number):boolean {

        if (duration <= 0)
            return false;

        const size = MatchDuration.ALL_DURATIONS_IN_NUMBERS.length;
        if (duration > MatchDuration.ALL_DURATIONS_IN_NUMBERS[size - 1])
            return true;

        if (MatchDuration.ALL_DURATIONS_IN_NUMBERS.includes(duration))
            return true;

        return false;
    }


}

