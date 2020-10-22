# What is it?
This module is responsible for managing the profiles

# Dependencies

- Android Room
- SQLite
- Dagger

## Usage

### Dagger

- Set up a @Component that at least
-- provides a _Context_
-- includes _ApiSDKModule.class_

Once that's set up, you can use _ProfileManagerWrapper_.


## Tools

### ProfileManagerWrapper interface

#### Methods

```
    /**
     * Get the profile associated to a profileId
     *
     * @return non null [IProfile] [Single] profile
     */
    fun getProfile(profileId: Long): Single<IProfile>

    /**
     * delete the profile associated to a profileId
     *
     * @return non null [Boolean] [Single] success
     */
    fun deleteProfile(profileId: Long): Single<Boolean>

    /**
     * Create a new profile
     *
     * @return non null [IProfile] [Single] profile created
     */
    fun createProfile(profile: IProfile): Single<IProfile>

    /**
     * Edit a profile
     *
     * @return non null [Boolean] [Single] success
     */
    fun editProfile(profile: IProfile): Single<Boolean>

    /**
     * Get the list of profiles
     *
     * @return non null [List] [IProfile] [Single] profiles fetched
     */
    fun getProfilesList(): Single<List<IProfile>>

```


### IProfile interface

Your profile object should implement the _IProfile_ to be able to use this module.


```
interface IProfile {
    val id: Long
    val firstName: String // firstname
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