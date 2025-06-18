package com.beardedbiz.billddifferent.util

import com.beardedbiz.billddifferent.R

fun getCategoryIconResId(category: String): Int {
    return when (category) {
        "Salary" -> R.drawable.ic_salary
        "Freelance / Side Income" -> R.drawable.ic_freelance_income
        "Investments / Dividends" -> R.drawable.ic_investments
        "Government Benefits" -> R.drawable.ic_government_benefits
        "Gifts / Reimbursements" -> R.drawable.ic_gifts
        "Rent / Mortgage" -> R.drawable.ic_rent
        "Insurance" -> R.drawable.ic_insurance
        "Utilities" -> R.drawable.ic_utilities
        "Internet / Cable" -> R.drawable.ic_internet
        "Groceries" -> R.drawable.ic_groceries
        "Clothing" -> R.drawable.ic_clothing
        "Dining / Takeout" -> R.drawable.ic_dining
        "Medical / Healthcare" -> R.drawable.ic_healthcare
        "Childcare / School" -> R.drawable.ic_childcare
        "Savings Goals" -> R.drawable.ic_savings
        "Entertainment / Hobbies" -> R.drawable.ic_entertainment
        "Subscriptions" -> R.drawable.ic_subscriptions
        "Emergency Fund" -> R.drawable.ic_emergency_fund
        "Fees / Bank Charges" -> R.drawable.ic_fees
        "Education / Tuition" -> R.drawable.ic_education
        "Other" -> R.drawable.ic_default_category
        "Select a Category" -> R.drawable.ic_default_category
        else -> R.drawable.ic_default_category
    }
}
