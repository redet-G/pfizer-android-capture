package org.dhis2.utils

import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.*
import java.util.*

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

class RulesUtilsProviderImpl(private val codeGenerator: CodeGenerator) : RulesUtilsProvider {

    private var currentFieldViewModels: HashMap<String, FieldViewModel>? = null


    override fun applyRuleEffects(fieldViewModels: MutableMap<String, FieldViewModel>,
                                  calcResult: Result<RuleEffect>,
                                  rulesActionCallbacks: RulesActionCallbacks) {

        calcResult.items().forEach {
            when (it.ruleAction()) {
                is RuleActionShowWarning -> showWarning(it.ruleAction() as RuleActionShowWarning, fieldViewModels, it.data())
                is RuleActionShowError -> showError(it.ruleAction() as RuleActionShowError, fieldViewModels, rulesActionCallbacks)
                is RuleActionHideField -> hideField(it.ruleAction() as RuleActionHideField, fieldViewModels, rulesActionCallbacks)
                is RuleActionDisplayText -> displayText(it.ruleAction() as RuleActionDisplayText, it, fieldViewModels)
                is RuleActionDisplayKeyValuePair -> displayKeyValuePair(it.ruleAction() as RuleActionDisplayKeyValuePair, it, fieldViewModels, rulesActionCallbacks)
                is RuleActionHideSection -> hideSection(it.ruleAction() as RuleActionHideSection, fieldViewModels, rulesActionCallbacks)
                is RuleActionAssign -> assign(it.ruleAction() as RuleActionAssign, it, fieldViewModels, rulesActionCallbacks)
                is RuleActionCreateEvent -> createEvent(it.ruleAction() as RuleActionCreateEvent, fieldViewModels, rulesActionCallbacks)
                is RuleActionSetMandatoryField -> setMandatory(it.ruleAction() as RuleActionSetMandatoryField, fieldViewModels)
                is RuleActionWarningOnCompletion -> warningOnCompletion(it.ruleAction() as RuleActionWarningOnCompletion, rulesActionCallbacks)
                is RuleActionErrorOnCompletion -> errorOnCompletion(it.ruleAction() as RuleActionErrorOnCompletion, rulesActionCallbacks)
                is RuleActionHideProgramStage -> hideProgramStage(it.ruleAction() as RuleActionHideProgramStage, rulesActionCallbacks)
                is RuleActionHideOption -> hideOption(it.ruleAction() as RuleActionHideOption, rulesActionCallbacks)
                is RuleActionHideOptionGroup -> hideOptionGroup(it.ruleAction() as RuleActionHideOptionGroup, rulesActionCallbacks)
                is RuleActionShowOptionGroup -> showOptionGroup(it.ruleAction() as RuleActionShowOptionGroup, rulesActionCallbacks)
                else -> rulesActionCallbacks.unsupportedRuleAction()
            }
        }

        if (currentFieldViewModels == null)
            currentFieldViewModels = HashMap()
        currentFieldViewModels!!.clear()
        currentFieldViewModels!!.putAll(fieldViewModels)
    }

    override fun applyRuleEffects(programStages: MutableMap<String, ProgramStage>, calcResult: Result<RuleEffect>) {
        calcResult.items().filter { it.ruleAction() is RuleActionHideProgramStage }.forEach {
            hideProgramStage(programStages, it.ruleAction() as RuleActionHideProgramStage)
        }
    }


    private fun showWarning(showWarning: RuleActionShowWarning,
                            fieldViewModels: MutableMap<String, FieldViewModel>, data: String) {

        val model = fieldViewModels[showWarning.field()]

        fieldViewModels[showWarning.field()]

        if (model != null)
            fieldViewModels[showWarning.field()] = model.withWarning(showWarning.content() + data)

    }

    private fun showError(showError: RuleActionShowError,
                          fieldViewModels: MutableMap<String, FieldViewModel>,
                          rulesActionCallbacks: RulesActionCallbacks) {
        val model = fieldViewModels[showError.field()]

        if (model != null)
            fieldViewModels[showError.field()] = model.withError(showError.content())

        rulesActionCallbacks.setShowError(showError, model)
    }

    private fun hideField(hideField: RuleActionHideField, fieldViewModels: MutableMap<String, FieldViewModel>,
                          rulesActionCallbacks: RulesActionCallbacks) {
        fieldViewModels.remove(hideField.field())
        rulesActionCallbacks.save(hideField.field(), null)
    }

    private fun displayText(displayText: RuleActionDisplayText,
                            ruleEffect: RuleEffect,
                            fieldViewModels: MutableMap<String, FieldViewModel>) {
        val uid = displayText.content()

        val displayViewModel = DisplayViewModel.create(uid, "",
                displayText.content() + ruleEffect.data(), "Display")
        fieldViewModels[uid] = displayViewModel
    }

    private fun displayKeyValuePair(displayKeyValuePair: RuleActionDisplayKeyValuePair,
                                    ruleEffect: RuleEffect,
                                    fieldViewModels: MutableMap<String, FieldViewModel>,
                                    rulesActionCallbacks: RulesActionCallbacks) {
        val uid = displayKeyValuePair.content()

        val displayViewModel = DisplayViewModel.create(uid, displayKeyValuePair.content(),
                ruleEffect.data(), "Display")
        fieldViewModels[uid] = displayViewModel
        rulesActionCallbacks.setDisplayKeyValue(displayKeyValuePair.content(), ruleEffect.data())

    }

    private fun hideSection(hideSection: RuleActionHideSection,
                            fieldViewModels: MutableMap<String, FieldViewModel>, rulesActionCallbacks: RulesActionCallbacks) {
        rulesActionCallbacks.setHideSection(hideSection.programStageSection())
        for (field in fieldViewModels.values) {
            if (field.programStageSection() == hideSection.programStageSection() && field.value() != null) {
                val uid = if (field.uid().contains(".")) field.uid().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] else field.uid()
                rulesActionCallbacks.save(uid, null)
            }
        }
    }

    private fun assign(assign: RuleActionAssign, ruleEffect: RuleEffect,
                       fieldViewModels: MutableMap<String, FieldViewModel>, rulesActionCallbacks: RulesActionCallbacks) {

        if (fieldViewModels[assign.field()] == null)
            rulesActionCallbacks.setCalculatedValue(assign.content(), ruleEffect.data())
        else {
            val value = fieldViewModels[assign.field()]!!.value()

            if (value == null || value != ruleEffect.data()) {
                rulesActionCallbacks.save(assign.field(), ruleEffect.data())
            }

            fieldViewModels.put(assign.field(), fieldViewModels[assign.field()]!!.withValue(ruleEffect.data()))!!.withEditMode(false)

        }
    }

    private fun createEvent(createEvent: RuleActionCreateEvent, fieldViewModels: MutableMap<String, FieldViewModel>, rulesActionCallbacks: RulesActionCallbacks) {
        //TODO: Create Event
    }

    private fun setMandatory(mandatoryField: RuleActionSetMandatoryField, fieldViewModels: MutableMap<String, FieldViewModel>) {
        val model = fieldViewModels[mandatoryField.field()]
        if (model != null)
            fieldViewModels[mandatoryField.field()] = model.setMandatory()
    }

    private fun warningOnCompletion(warningOnCompletion: RuleActionWarningOnCompletion, rulesActionCallbacks: RulesActionCallbacks) {
        rulesActionCallbacks.setMessageOnComplete(warningOnCompletion.content(), true)
    }

    private fun errorOnCompletion(errorOnCompletion: RuleActionErrorOnCompletion, rulesActionCallbacks: RulesActionCallbacks) {
        rulesActionCallbacks.setMessageOnComplete(errorOnCompletion.content(), false)
    }


    private fun hideProgramStage(hideProgramStage: RuleActionHideProgramStage, rulesActionCallbacks: RulesActionCallbacks) {
        rulesActionCallbacks.setHideProgramStage(hideProgramStage.programStage())
    }

    private fun hideProgramStage(programStages: MutableMap<String, ProgramStage>, hideProgramStage: RuleActionHideProgramStage) {
        programStages.remove(hideProgramStage.programStage())
    }

    private fun hideOption(hideOption: RuleActionHideOption,
                           rulesActionCallbacks: RulesActionCallbacks) {
        rulesActionCallbacks.setOptionToHide(hideOption.option())
    }

    private fun hideOptionGroup(hideOptionGroup: RuleActionHideOptionGroup,
                                rulesActionCallbacks: RulesActionCallbacks) {
        rulesActionCallbacks.setOptionGroupToHide(hideOptionGroup.optionGroup(),true)
    }

    private fun showOptionGroup(showOptionGroup: RuleActionShowOptionGroup,
                                rulesActionCallbacks: RulesActionCallbacks) {
        rulesActionCallbacks.setOptionGroupToHide(showOptionGroup.optionGroup(),false, showOptionGroup.field())
    }
}
