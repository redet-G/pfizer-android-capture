package org.dhis2.utils.custom_views.orgUnitCascade;

import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class OrgUnitItem {

    private final OrganisationUnitCollectionRepository ouRepo;
    private OrganisationUnit organisationUnit;
    private OrganisationUnitLevel organisationUnitLevel;
    private int level;
    private String uid;
    private String parentUid;
    private boolean hasCaptureOrgUnits;
    private final OrganisationUnit.Scope ouScope;


    public OrgUnitItem(OrganisationUnitCollectionRepository ouRepo, OrgUnitCascadeDialog.OUSelectionType ouSelectionType) {
        this.ouRepo = ouRepo;
        this.ouScope = ouSelectionType == OrgUnitCascadeDialog.OUSelectionType.SEARCH ? OrganisationUnit.Scope.SCOPE_TEI_SEARCH : OrganisationUnit.Scope.SCOPE_DATA_CAPTURE;

    }

    public boolean canCaptureData() {
        getLevelOrgUnits();
        return hasCaptureOrgUnits;
    }

    public List<Trio<String, String, Boolean>> getLevelOrgUnits() {

        OrganisationUnitCollectionRepository finalOuRepo = ouRepo;
        if (!isEmpty(parentUid))
            finalOuRepo = finalOuRepo.byParentUid().eq(parentUid);

        List<OrganisationUnit> orgUnitList = finalOuRepo.get();
        if (orgUnitList.isEmpty())//When parent is set and list is empty the ou has not been downloaded, we have to get it from the uidPath
            orgUnitList = ouRepo.get();
        List<OrganisationUnit> captureOrgUnits = finalOuRepo.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get();

        Map<String, Trio<String, String, Boolean>> menuOrgUnits = new HashMap<>();
        for (OrganisationUnit ou : orgUnitList) {
            String[] uidPath = ou.path().replaceFirst("/", "").split("/");
            String[] namePath = ou.displayNamePath().replaceFirst("/", "").split("/");
            if (uidPath.length >= level && !menuOrgUnits.containsKey(uidPath[level - 1]) && (isEmpty(parentUid) || (level > 1 && uidPath[level - 2].equals(parentUid)))) {
                boolean canCapture = ouRepo.byOrganisationUnitScope(ouScope).uid(uidPath[level - 1]).exists();
                menuOrgUnits.put(uidPath[level - 1],
                        Trio.create(
                                uidPath[level - 1],
                                namePath[level - 1],
                                canCapture));
                if (canCapture)
                    hasCaptureOrgUnits = true;
            }
        }
        List<Trio<String, String, Boolean>> menuOrgUnitList = new ArrayList<>(menuOrgUnits.values());
        Collections.sort(menuOrgUnitList, (ou1, ou2) -> ou1.val1().compareTo(ou2.val1()));
        return menuOrgUnitList;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getParentUid() {
        return parentUid;
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public OrganisationUnit getOrganisationUnit() {
        return organisationUnit;
    }

    public void setOrganisationUnit(OrganisationUnit organisationUnit) {
        this.organisationUnit = organisationUnit;
    }

    public OrganisationUnitLevel getOrganisationUnitLevel() {
        return organisationUnitLevel;
    }

    public void setOrganisationUnitLevel(OrganisationUnitLevel organisationUnitLevel) {
        this.organisationUnitLevel = organisationUnitLevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
