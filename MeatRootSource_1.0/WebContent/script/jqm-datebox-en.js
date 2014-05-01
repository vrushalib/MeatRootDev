/* English/UK initialisation for the jqm datebox */
	jQuery.extend(jQuery.mobile.datebox.prototype.options.lang, {
        'en': {
            setDateButtonLabel: "Set Date",
            setTimeButtonLabel: "Set Time",
            setDurationButtonLabel: "Set Duration",
            calTodayButtonLabel: "Jump to Today",
            titleDateDialogLabel: "Set Date",
            titleTimeDialogLabel: "Set Time",
            daysOfWeek: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
            daysOfWeekShort: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
            monthsOfYear: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
            monthsOfYearShort: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            durationLabel: ['Days', 'Hours', 'Minutes', 'Seconds'],
            durationDays: ['Day', 'Days'],
            tooltip: "Open Date Picker",
            nextMonth: "Next Month",
            prevMonth: "Previous Month",
            timeFormat: 12,
            headerFormat: '%A, %B %-d, %Y',
            dateFieldOrder: ['d', 'm', 'y'],
            timeFieldOrder: ['h', 'i', 'a'],
            slideFieldOrder: ['y', 'm', 'd'],
            dateFormat: '%d/%m/%Y',
            useArabicIndic: false,
            isRTL: false,
            calStartDay: 0,
            clearButton: 'clear',
            durationOrder: ['d', 'h', 'i', 's'],
            meridiem: ['AM', 'PM'],
            timeOutput: '%k:%M', // 12hr: '%l:%M %p', 24hr: '%k:%M'
            durationFormat: '%Dd %DA, %Dl:%DM:%DS'
        }
    });
