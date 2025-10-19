
package com.screenmask

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RuleManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("rules", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getRules(): List<Rule> {
        val json = prefs.getString("rules_list", "[]") ?: "[]"
        val type = object : TypeToken<List<Rule>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addRule(rule: Rule) {
        val rules = getRules().toMutableList()
        rules.add(rule)
        saveRules(rules)
    }

    fun updateRule(rule: Rule) {
        val rules = getRules().toMutableList()
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index != -1) {
            rules[index] = rule
            saveRules(rules)
        }
    }

    fun deleteRule(id: Long) {
        val rules = getRules().toMutableList()
        rules.removeAll { it.id == id }
        saveRules(rules)
    }

    private fun saveRules(rules: List<Rule>) {
        val json = gson.toJson(rules)
        prefs.edit().putString("rules_list", json).apply()
    }
}