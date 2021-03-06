package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.AddressBookChangedEvent;
import seedu.address.commons.events.ui.RemoveSelectedTeamEvent;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.NoPlayerException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;
import seedu.address.model.team.Team;
import seedu.address.model.team.TeamName;
import seedu.address.model.team.exceptions.DuplicateTeamException;
import seedu.address.model.team.exceptions.TeamNotFoundException;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final FilteredList<Person> filteredPersons;

    private final UserPrefs userPrefs;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, UserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);
        this.userPrefs = userPrefs;

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        addressBook.resetData(newData);
        indicateAddressBookChanged();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateAddressBookChanged() {
        raise(new AddressBookChangedEvent(addressBook));
    }

    @Override
    public synchronized void deletePerson(Person target) throws PersonNotFoundException {
        addressBook.removePerson(target);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void addPerson(Person person) throws DuplicatePersonException {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        indicateAddressBookChanged();
    }

    @Override
    public void updatePerson(Person target, Person editedPerson)
            throws DuplicatePersonException, PersonNotFoundException {
        requireAllNonNull(target, editedPerson);
        addressBook.updatePerson(target, editedPerson);
        indicateAddressBookChanged();
    }

    //@@author lohtianwei
    @Override
    public void sortPlayers(String field, String order) throws NoPlayerException {
        addressBook.sortPlayersBy(field, order);
        indicateAddressBookChanged();
    }

    public UserPrefs getUserPrefs() {
        return userPrefs;
    }

    public void lockAddressBookModel() {
        getUserPrefs().lockAddressBook();
    }

    public void unlockAddressBookModel() {
        getUserPrefs().unlockAddressBook();
    }

    public boolean getLockState() {
        return getUserPrefs().getAddressBookLockState();
    }
    //@@author

    @Override
    public void deleteTag(Tag tag) {
        addressBook.removeTag(tag);
        indicateAddressBookChanged();
    }

    //@@author jordancjq
    @Override
    public synchronized void createTeam(Team team) throws DuplicateTeamException {
        addressBook.createTeam(team);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void assignPersonToTeam(Person person, TeamName teamName) throws DuplicatePersonException {
        addressBook.assignPersonToTeam(person, teamName);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void unassignPersonFromTeam(Person person) throws TeamNotFoundException {
        addressBook.unassignPersonFromTeam(person);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void removeTeam(TeamName teamName) throws TeamNotFoundException {
        requireNonNull(teamName);
        raise(new RemoveSelectedTeamEvent(teamName));
        addressBook.removeTeam(teamName);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void renameTeam(Team targetTeam, TeamName updatedTeamName) {
        requireAllNonNull(targetTeam, updatedTeamName);
        addressBook.renameTeam(targetTeam, updatedTeamName);
        indicateAddressBookChanged();
    }

    //@@author Codee
    @Override
    public boolean setTagColour(Tag tag, String colour) {
        ObservableList<Tag> allTags = addressBook.getTagList();
        boolean isTagValid = false;
        for (Tag t : allTags) {
            if (t.getTagName().equals(tag.getTagName())) {
                isTagValid = true;
                break;
            }
        }
        if (!isTagValid) {
            return false;
        }
        addressBook.setTagColour(tag, colour);
        indicateAddressBookChanged();
        return isTagValid;
    }

    //@@author
    @Override
    public ObservableList<Team> getInitTeamList() {
        return addressBook.getTeamList();
    }

    //=========== Filtered Person List Accessors =============================================================
    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return FXCollections.unmodifiableObservableList(filteredPersons);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    //@@author jordancjq
    @Override
    public void updateFilteredPersonList(TeamName targetTeam) throws TeamNotFoundException {
        requireNonNull(targetTeam);

        List<Team> teamList = addressBook.getTeamList();

        if (teamList.stream().anyMatch(target -> target.getTeamName().equals(targetTeam))) {
            filteredPersons.setPredicate(t -> t.getTeamName().equals(targetTeam));
        } else {
            throw new TeamNotFoundException();
        }
    }

    //@@author
    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return addressBook.equals(other.addressBook)
                && filteredPersons.equals(other.filteredPersons);
    }

}
