# What is it?
This module is responsible for managing the brushings for a profile

# Dependencies

- Android Room
- SQLite
- Dagger

## Usage

### Dagger

- Set up a @Component that at least
-- provides a _Context_
-- includes _ApiSDKModule.class_

Once that's set up, you can use _BrushingManagerWrapper_.

## Tools

### BrushingManagerWrapper interface

#### Methods

```
 /**
      * Add a new brushing to a profile
      *
      * @return non null [IBrushing] [Single] brushing
      */
     fun addBrushing(IBrushing: IBrushing, profile: IProfile): Single<IBrushing>

     /**
      * Get all the brushings for a profile
      *
      * @return non null [IBrushing] [List] [Single] list of brushings stored
      */
     fun getBrushings(profileId: Long): Single<List<IBrushing>>

     /**
      * Get all the brushings for a profile since a given date
      *
      * @return non null [IBrushing] [List] [Single] list of brushings stored since this date
      */
     fun getBrushingsSince(startTime: ZonedDateTime, profileId: Long): Single<List<IBrushing>>

     /**
      * Get the latest brushing sessions for a profile
      * Throw an exception if there is no existing brushing for this user
      * @return non null [IBrushing] [Single] last brushing if exist
      */
     fun getLastBrushingSession(profileId: Long): Single<IBrushing>

     /**
      * delete a  brushing for a profile
      *
      * @return non null [Completable]
      */
     fun deleteBrushing(profileId: Long, brushing: IBrushing): Completable

```


### IBrushing interface

Your Brushing object should implement the _IBrushing_ interface and
your profile object should implement the _IProfile_ to be able to use this module.


```
interface IBrushing {
    val duration: Long // duration of the brushing
    val goalDuration: Int // goal duration set in the profile
    val timestamp: Long // timestamp of the brushing
    val quality: Int // quality of the brushing, value on 100
    val processedData: String? // processed data during the brushing

    val date: ZonedDateTime
        get() = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

    fun hasProcessedData(): Boolean {
        return !processedData.isNullOrEmpty()
    }
}


```


```
interface IProfile {
    val id: Long
    val firstName: String // first name
    val gender: Gender // gender of the user
    val handedness: Handedness // hand used by the user
    val brushingGoalTime: Int // brushing goal time
    val createdDate: String // date of the profile creation
    val birthday: LocalDate? // date of birth of the user

    /**
     * Check if the current profile is a Male
     *
     * @return true if it's a Male, false otherwise [Gender]
     */
    fun isMale() = gender == Gender.MALE

    /**
     * Check if the current profile is a Right handed
     *
     * @return true if the user is Right Handed, false otherwise [Handedness]
     */
    fun isRightHanded() = handedness == Handedness.RIGHT_HANDED

    /**
     * Get the age of the user given the birthday atribute.
     * Return -1 if the birthday has not been defined
     *
     * @return the age of the user [Int]
     */
    fun getAgeFromBirthday() = birthday?.let { KolibreeUtils.getAgeFromBirthDate(it) } ?: -1
```

This snippet of code is written in kotlin.