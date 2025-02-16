/* eslint-disable */


import { logger } from "firebase-functions/v1";
import { FetchMatchesData } from "../models/FetchMatchesData";
import { ElementNotFoundException } from "../exceptions/ElementNotFoundException";
import {Match} from "../models/Match";
import {User} from "../models/user/User";
import {MatchFilter} from "../models/MatchFilter";
import {HasTerrainTypes} from "../models/constants/HasTerrainType";
import {UserLevelBasedOnSport} from "../models/user/UserLevelBasedOnSport";

export class FetchMatchesService {


    constructor() {}

    fetchMatchesService(allMatches:Match[], user:User, fetchMatchesData:FetchMatchesData):Match[]|null {

        if (allMatches.length == 0){
            logger.info("if documents.empty",allMatches);
            return null;//remember the null here
        }


        // Filter locations within the given radius.
        const withinRadiusAndOtherFilters:Match[] = allMatches.filter(match => {

            const approvedLevels = match.getLevels();
            const userLevelOnThatSport: UserLevelBasedOnSport | undefined = user.getUserLevelBasedOnSport().get(match.getSport());
            if (userLevelOnThatSport !== undefined){

                const userLevel:number = userLevelOnThatSport.getLevel();
                if (!approvedLevels.includes(userLevel))
                    return false;
            }

            const allGood:boolean = this.applyMatchFilter(match, fetchMatchesData.getMatchFilter());

            if (!allGood)
                return false;


            return this.isWithinRadius(user.getLatitude(), user.getLongitude(), match.getLatitude(), match.getLongitude(), user.getRadiusSearchInM());
        });
        logger.info("withinRadiusAndOtherFilters",withinRadiusAndOtherFilters);

        if (withinRadiusAndOtherFilters.length == 0){
            return null;//remember the null here
        }


        // Sort by createdAt and limit results.
        const sortedByDate:Match[] = withinRadiusAndOtherFilters.sort((o1, o2) => {

            if (o1.getMatchDateInUTC() - o2.getMatchDateInUTC() == 0){
                if (o1.getId() < o2.getId()){
                    return 1;
                }else{
                    return -1;
                }
            }

            return o1.getMatchDateInUTC() - o2.getMatchDateInUTC();
        });
        logger.info("sortedByDate",sortedByDate);

        const filteredByDate:Match[] = this.filterByDateOnSortedList(sortedByDate, fetchMatchesData.getDateBeginForPaginationUTC(), fetchMatchesData.getDateLastForPaginationUTC());
        logger.info("filteredByDate",filteredByDate);


        const matchesAfterNow:Match[] = this.excludeMatchesThatAreInPast(filteredByDate);
        logger.info("matchesAfterNow",matchesAfterNow);


        const paginatedList:Match[] = this.pagination(matchesAfterNow,fetchMatchesData.getLastVisibleDocumentMatchId(),fetchMatchesData.getLimit());
        logger.info("paginatedList",paginatedList);


        return paginatedList;

    }

    private applyMatchFilter(match: Match, matchFilter: MatchFilter) {

        if (!matchFilter.isEnabled())
            return true;

        const members:number = match.getUsersInChat().length;
        if (!(members >= matchFilter.getFromMembers() && members <= matchFilter.getToMembers()))
            return false;

        let allGood:boolean = false;
        if (matchFilter.isYesTerrain() && match.getHasTerrainType() === HasTerrainTypes.I_HAVE_CERTAIN_TERRAIN)
            allGood = true;

        if (matchFilter.isMaybeTerrain() && match.getHasTerrainType() === HasTerrainTypes.I_HAVE_NOT_CERTAIN_TERRAIN)
            allGood = true;

        if (matchFilter.isNoTerrain() && match.getHasTerrainType() === HasTerrainTypes.I_DONT_HAVE_TERRAIN)
            allGood = true;

        return allGood;
    }

    excludeMatchesThatAreInPast(matchesList:any[]):any[]|[] {

        const currentTime = Date.now();

        // Finding the index of the first timestamp that is in the future
        const firstFutureIndex = matchesList.findIndex(match => match.matchDateInUTC > currentTime);

        // If there's at least one timestamp in the future, slice the array
        const futureLocations:any[]|[] = firstFutureIndex !== -1 ? matchesList.slice(firstFutureIndex) : [];

        return futureLocations

    }

    pagination(sortedLocations:any[], lastVisibleMatchId:string, limit:number):any[] {

        if (sortedLocations.length === 0){
            return [];
        }

        if (lastVisibleMatchId === "") {
            return sortedLocations.slice(0, limit);
        }

        // Step 1: Locate the index of the last visible ID
        const lastVisibleIndex = sortedLocations.findIndex(match => match.id === lastVisibleMatchId);

        if (lastVisibleIndex == -1){
            throw new ElementNotFoundException('match with id ' + lastVisibleMatchId + ' was not found');
        }
        // Step 2: Slice the array for the next page
        const nextPageStartIndex = lastVisibleIndex + 1; // Start index of the next page

        const nextPage = sortedLocations.slice(nextPageStartIndex, nextPageStartIndex + limit);


        return nextPage;

    }



    isWithinRadius(userLat: number, userLon: number, matchLat: number, matchLon: number, radiusSearchInM: number): boolean {
        // Calculating distance using the corrected formula
        const distanceX = (matchLat - userLat);
        const distanceY = (matchLon - userLon) * Math.cos((Math.PI / 180) * (userLat + matchLat) / 2);

        const distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        // Convert the distance to meters (assuming that 1 degree is approximately 111km)
        const distanceInMeters = distance * 111000;

        return distanceInMeters <= radiusSearchInM;
    }

    filterByDateOnSortedList(sortedByDate:any[], dateBeginForPaginationUTC:number, dateLastForPaginationUTC:number):any[] | []{

        if (dateBeginForPaginationUTC == 0 || dateLastForPaginationUTC == 0){
            return sortedByDate;
        }


        const lowerBound = dateBeginForPaginationUTC;
        const upperBound = dateLastForPaginationUTC;

        const filteredNumbers = [];
        for (const match of sortedByDate) {

            if (match.matchDateInUTC > upperBound) {
                break; // Exit the loop early since the array is sorted
            }
            if (match.matchDateInUTC >= lowerBound) {
                filteredNumbers.push(match);
            }
        }


        return filteredNumbers;
    }



}




