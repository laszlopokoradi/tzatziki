package io.nimbly.tzatziki.format

import io.nimbly.tzatziki.AbstractTestCase

class CopyPasteTests  : AbstractTestCase() {

    fun testCopy() {

        // language=feature
        configure("""
            Feature: Tzatziki y Cucumber
              Scenario Outline: Auto formating
                When I enter any character into <NAF> or <Ready> or <Details>
                Then The Cucumber table is formatted !
                Examples:
                  | NAF | Ready | Details |
                  | 78  | Yes   |         |
                  | 79  | No    | D2      |
                Then FInished !
            """)

        selectAsColumn("| NAF |", "| D2      ")
        copy()

        checkClipboard("""
                Ready	Details
                Yes	
                No	D2""")
    }

    fun testCopyPaste() {

        // language=feature
        configure("""
            Feature: Tzatziki y Cucumber
              Scenario Outline: Auto formating
                When I enter any character into <NAF> or <Ready> or <Details>
                Then The Cucumber table is formatted !
                Examples: One
                  | NAF | Ready | Details |
                  | 78  | Yes   |         |
                  | 79  | No    | D2      |
                Examples: Two
                  | Title | Size |
                  | A     | 22   |
                  | C     | 144  |
                Then Finished !
                        """)

        selectAsColumn("|", "| No    ")
        copy()

        setCursor("Title | Si")
        paste()

        // language=feature
        checkContent("""
            Feature: Tzatziki y Cucumber
              Scenario Outline: Auto formating
                When I enter any character into <NAF> or <Ready> or <Details>
                Then The Cucumber table is formatted !
                Examples: One
                  | NAF | Ready | Details |
                  | 78  | Yes   |         |
                  | 79  | No    | D2      |
                Examples: Two
                  | Title | NAF | Ready |
                  | A     | 78  | Yes   |
                  | C     | 79  | No    |
                Then Finished !
            """)

        setCursor("| C     | 79  | No")
        paste()

        // language=feature
        checkContent("""
            Feature: Tzatziki y Cucumber
              Scenario Outline: Auto formating
                When I enter any character into <NAF> or <Ready> or <Details>
                Then The Cucumber table is formatted !
                Examples: One
                  | NAF | Ready | Details |
                  | 78  | Yes   |         |
                  | 79  | No    | D2      |
                Examples: Two
                  | Title | NAF | Ready |       |
                  | A     | 78  | Yes   |       |
                  | C     | 79  | NAF   | Ready |
                  |       |     | 78    | Yes   |
                  |       |     | 79    | No    |
                Then Finished !
            """)
    }
}

