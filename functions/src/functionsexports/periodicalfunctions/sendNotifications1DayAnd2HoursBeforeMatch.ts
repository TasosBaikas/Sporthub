/* eslint-disable */


import {BatchRepository} from "../../repository/BatchRepository";

const functions = require("firebase-functions");
import { logger } from "firebase-functions";
import { Batch } from "../../models/Batch";
import {SportConstants} from "../../models/constants/SportConstants";
import {UserNotificationsRepository} from "../../repository/UserNotificationsRepository";
import {Match} from "../../models/Match";
import {UserNotification} from "../../models/user/usernotification/UserNotification";
import {UserFcmRepository} from "../../repository/UserFcmRepository";

const batchRepository: BatchRepository = new BatchRepository();
const userNotificationsRepository: UserNotificationsRepository = new UserNotificationsRepository();
const userFcmRepository: UserFcmRepository = new UserFcmRepository();

export const sendNotifications1DayAnd2HoursBeforeMatch = functions.region('europe-west1')
.pubsub.schedule('*/15 * * * *').timeZone('UTC').onRun(async (context: any) => {

    const sportsList = SportConstants.SPORTSLIST;

    const promiseUserNotificationArray: Promise<{ match: Match, userNotification:  (UserNotification | undefined)[] }>[] = [];

    const allBatches: Batch[] = await batchRepository.getBatchesPromise(sportsList);
    allBatches.forEach(batch => {

        const matches: Match[] = findMatchesThatWeNeedToSendNotification(batch);
        logger.info("matches", matches);

        matches.forEach(match => { // Added this loop to iterate over matches

            const userNotificationsPromises: Promise<{ match: Match, userNotification:  (UserNotification | undefined)[] }> = usersThatHaveEnabledNotificationsPromise(match);

            promiseUserNotificationArray.push(userNotificationsPromises);
        });

    });


    const matchesAndUsersNotificationsSettings: { match: Match, userNotification: (UserNotification | undefined)[] }[] = await Promise.all(promiseUserNotificationArray);

    logger.info("matchesAndUsersNotificationsSettings",matchesAndUsersNotificationsSettings);

    const usersToSendTheNotifications:{ match: Match, usersThatShouldGetNotification: string[] | [] }[] = [];
    for (const matchAndNotif of matchesAndUsersNotificationsSettings) {
        const usersThatShouldGetNotification: string[] = [];

        for (const userNotification of matchAndNotif.userNotification) {
            if (userNotification === undefined)
                continue;

            if (!userNotification.getGeneralNotificationOptions().getNotificationsBeforeMatch())
                continue;

            const chatNotificationsSettings:any | undefined = userNotification.getNotificationsBasedOnChats().get(matchAndNotif.match.getId());
            logger.info("chatNotificationsSettings",chatNotificationsSettings)

            if (chatNotificationsSettings === undefined){
                usersThatShouldGetNotification.push(userNotification.getUserId());
                continue;
            }

            if (chatNotificationsSettings.notificationsBeforeMatch === true){
                usersThatShouldGetNotification.push(userNotification.getUserId());
                continue;
            }
        }

        logger.info("{ match: matchAndNotif.match, usersThatShouldGetNotification:usersThatShouldGetNotification }",{ match: matchAndNotif.match, usersThatShouldGetNotification:usersThatShouldGetNotification })

        usersToSendTheNotifications.push({ match: matchAndNotif.match, usersThatShouldGetNotification:usersThatShouldGetNotification });
    }

    if (usersToSendTheNotifications.length === 0){
        return null;
    }


    const promiseArray: Promise<{ match: Match, tokens: string[]}>[] = [];

    usersToSendTheNotifications.forEach(({ match, usersThatShouldGetNotification }:
        { match: any, usersThatShouldGetNotification: string[] }) => { // Corrected the types here

        const matchWithTokensPromise: Promise<{ match: Match, tokens: string[] }> = promisesForEachMatchGetChatUsersFcmTokens(match,usersThatShouldGetNotification);

        if (matchWithTokensPromise === undefined){
            return;
        }

        promiseArray.push(matchWithTokensPromise);
    });

    const matchAndTokensArray: { match: Match, tokens: string[] }[] = await Promise.all(promiseArray);

    sendNotificationToUsers(matchAndTokensArray);

    return null;
});



function findMatchesThatWeNeedToSendNotification(batchInstance: Batch):any[] {
    const serverTimeUTC = Date.now(); // This will return current time in milliseconds (epoch)

    const oneDayInMIlli:number = 86400000;
    const minutes7_5InMilli:number = 450000;
    const hours2InMilli:number = 7200000;

    const matchesToSendNotification:any[] = [];
    batchInstance.getMatchesList().forEach((match:any) => {

        const matchStart:number = match.matchDateInUTC;

        if (matchStart > serverTimeUTC + oneDayInMIlli - minutes7_5InMilli && matchStart < serverTimeUTC + oneDayInMIlli + minutes7_5InMilli){
            matchesToSendNotification.push(match);
            logger.info("findMatchesThatWeNeedToSendNotification","match",match);

        }else if (matchStart > serverTimeUTC + hours2InMilli - minutes7_5InMilli && matchStart < serverTimeUTC + hours2InMilli + minutes7_5InMilli){
            matchesToSendNotification.push(match);
            logger.info("findMatchesThatWeNeedToSendNotification","match",match);

        }
        logger.info("serverTimeUTC + oneDayInMIlli - minutes15InMilli","result ->",serverTimeUTC + oneDayInMIlli - minutes7_5InMilli);
        logger.info("serverTimeUTC + oneDayInMIlli + minutes15InMilli","result ->",serverTimeUTC + oneDayInMIlli + minutes7_5InMilli);
        logger.info(" serverTimeUTC + hours2InMilli - minutes15InMilli","result ->", serverTimeUTC + hours2InMilli - minutes7_5InMilli);
        logger.info("serverTimeUTC + hours2InMilli + minutes15InMilli","result ->",serverTimeUTC + hours2InMilli + minutes7_5InMilli);

    })

    return matchesToSendNotification;
}

async function promisesForEachMatchGetChatUsersFcmTokens(match: Match, usersThatShouldGetNotification: string[]): Promise<{ match: Match, tokens: string[] }> {

    const userPromises: Promise<string[]>[] = usersThatShouldGetNotification.map((userId: string) => {
        return userFcmRepository.getUserFcmTokens(userId);
    });

    const fcmTokens:string[][] = await Promise.all(userPromises);
    return { match, tokens: fcmTokens.flat() };
}


function sendNotificationToUsers(matchAndTokensArray: { match: Match; tokens: string[] }[]) {

    matchAndTokensArray.forEach(async (matchAndTokens: { match: Match; tokens: string[]; }) => {

        const payload = {
            data: {
                pushNotificationType: "reminderUpComingMatch",
                match: JSON.stringify(matchAndTokens.match),
            },
            android: {
                priority: 'high',
            },
            apns: {
                headers: {
                    'apns-priority': '10',
                },
            },
          };

          try {
              // Use sendEachForMulticast to send the notification to all tokens
              const response = await userFcmRepository.sendEachForMulticast(matchAndTokens.tokens,payload);

              console.log(`Sent ${response.successCount} messages successfully.`);
              if (response.failureCount > 0) {
                  console.error("Some messages failed:", response.responses);
              }
          } catch (error) {
              console.error("Error sending notification:", error);
          }
    });

  }

async function usersThatHaveEnabledNotificationsPromise(match: Match): Promise<{ match: Match, userNotification: (UserNotification | undefined)[]}> {

    const userNotificationPromises: Promise<UserNotification | undefined>[] = match.getUsersInChat().map((userId: string) => {
        return userNotificationsRepository.getUserNotificationsByIdPromise(userId);
    });

    const userNotifications: (UserNotification | undefined)[] = await Promise.all(userNotificationPromises);

    return { match: match, userNotification: userNotifications }; // Use the current match

}

