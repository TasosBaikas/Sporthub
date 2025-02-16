/* eslint-disable */


import {ObjectWithId} from "../../interfaces/ObjectWithId";
import {Comparator} from "../../interfaces/Comparator";

export class ListOperations{


    public static updateOrAddAndSort<T extends ObjectWithId>(newMatch:T , currentList:T[] , comparator: Comparator<T> ):void {
        let isMatchFound:boolean = false;

        for (let i = 0; i < currentList.length; i++) {
            if (currentList[i].getId() === newMatch.getId()) {  // Assuming Match has a getId() method
                currentList[i] = newMatch;  // Replace with the newMatch
                isMatchFound = true;
                break;
            }
        }

        if (!isMatchFound) {
            currentList.push(newMatch);
        }

        currentList.sort((o1,o2) => comparator.compare(o1,o2));
    }

}
